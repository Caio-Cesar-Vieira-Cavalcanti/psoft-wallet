package com.ufcg.psoft.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ufcg.psoft.commerce.dto.asset.AssetActivationPatchRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetQuotationUpdateDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;
import com.ufcg.psoft.commerce.exception.asset.InvalidAssetTypeException;
import com.ufcg.psoft.commerce.exception.asset.InvalidQuotationVariationException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.asset.AssetServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AssetServiceUnitTests {

    private AssetRepository assetRepository;
    private AssetTypeRepository assetTypeRepository;
    private AdminService adminService;
    private AssetService assetService;
    private ModelMapper modelMapper;

    private UUID assetId;
    private AssetModel asset;

    private AssetType mockStockType() {
        Stock stock = new Stock();
        stock.setName("STOCK");
        stock.setId(2L);
        return stock;
    }

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        assetTypeRepository = mock(AssetTypeRepository.class);
        adminService = mock(AdminService.class);
        modelMapper = new ModelMapper();

        assetService = new AssetServiceImpl();
        ReflectionTestUtils.setField(assetService, "assetRepository", assetRepository);
        ReflectionTestUtils.setField(assetService, "assetTypeRepository", assetTypeRepository);
        ReflectionTestUtils.setField(assetService, "adminService", adminService);
        ReflectionTestUtils.setField(assetService, "modelMapper", modelMapper);

        assetId = UUID.randomUUID();
        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .assetType(mockStockType())
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        doNothing().when(adminService).validateAdmin(anyString(), anyString());
    }

    @Test
    void testUpdateQuotation_Success() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(105.0)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(105.0, response.getQuotation());
        verify(assetRepository).save(asset);
    }

    @Test
    void testUpdateQuotation_ThrowsAssetNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(assetRepository.findById(unknownId)).thenReturn(Optional.empty());

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(AssetNotFoundException.class, () -> assetService.updateQuotation(unknownId, dto));
    }

    @Test
    void testUpdateQuotation_ThrowsInvalidQuotationVariationException() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(100.5)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_InvalidAssetType_ShouldThrowException() {
        AssetType invalidType = new AssetType("INVALID_TYPE") {
            @Override
            public double taxCalculate(double profit) {
                return 0;
            }
        };

        asset.setAssetType(invalidType);

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(120.0)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(InvalidAssetTypeException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_MinimumValidVariation_ShouldUpdate() {
        double current = 100.0;
        double newQuotation = current * 1.01;

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(newQuotation)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(newQuotation, response.getQuotation());
    }

    @Test
    void testUpdateQuotation_MinimumValidNegativeVariation_ShouldUpdate() {
        double current = 100.0;
        double newQuotation = current * 0.99;

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(newQuotation)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(newQuotation, response.getQuotation());
    }

    @Test
    void testUpdateQuotation_InvalidPositiveVariation_ShouldThrowException() {
        double current = 100.0;
        double newQuotation = current * 1.005;

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(newQuotation)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_InvalidNegativeVariation_ShouldThrowException() {
        double current = 100.0;
        double newQuotation = current * 0.995;

        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(newQuotation)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_UnauthorizedAdminAccess_ShouldThrowException() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail("invalid@example.com")
                .adminAccessCode("wrong-code")
                .build();

        doThrow(new UnauthorizedUserAccessException())
                .when(adminService)
                .validateAdmin("invalid@example.com", "wrong-code");

        assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        verify(assetRepository, never()).save(any());
    }

    @Test
    void testUpdateQuotation_UnauthorizedAdminAccess_EvenIfQuotationValid_ShouldThrowException() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(150.0)
                .adminEmail("invalid@example.com")
                .adminAccessCode("wrong-code")
                .build();

        doThrow(new UnauthorizedUserAccessException())
                .when(adminService)
                .validateAdmin("invalid@example.com", "wrong-code");

        assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        verify(assetRepository, never()).save(any());
    }

    @Test
    void testGetAllAssets() {
        AssetModel asset2 = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Second Asset")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(mockStockType())
                .build();

        List<AssetModel> mockAssets = List.of(asset, asset2);

        when(assetRepository.findAll()).thenReturn(mockAssets);

        List<AssetResponseDTO> result = assetService.getAllAssets();

        assertEquals(2, result.size());
        assertEquals("Test Asset", result.get(0).getName());
        assertEquals("Second Asset", result.get(1).getName());
    }

    @Test
    void testGetAssetById_ReturnsCorrectAsset() {
        AssetResponseDTO response = assetService.getAssetById(assetId);

        assertNotNull(response);
        assertEquals(asset.getId(), response.getId());
        assertEquals(asset.getName(), response.getName());
    }

    @Test
    void testGetActiveAssets_ReturnsOnlyActiveAssets() {
        AssetModel activeAsset1 = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Active Asset 1")
                .quotation(120.0)
                .quotaQuantity(500.0)
                .isActive(true)
                .assetType(mockStockType())
                .build();

        AssetModel inactiveAsset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Inactive Asset")
                .quotation(130.0)
                .quotaQuantity(700.0)
                .isActive(false)
                .assetType(mockStockType())
                .build();

        AssetModel activeAsset2 = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Active Asset 2")
                .quotation(150.0)
                .quotaQuantity(900.0)
                .isActive(true)
                .assetType(mockStockType())
                .build();

        List<AssetModel> allAssets = List.of(activeAsset1, inactiveAsset, activeAsset2);

        when(assetRepository.findAll()).thenReturn(allAssets);

        List<AssetResponseDTO> result = assetService.getActiveAssets();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getName().equals("Active Asset 1")));
        assertTrue(result.stream().anyMatch(a -> a.getName().equals("Active Asset 2")));
    }

    @Test
    void testGetActiveAssetsByAssetType() {
        AssetType mockAssetType = mock(AssetType.class);

        AssetModel asset1 = new AssetModel();
        asset1.setName("Active Asset");
        asset1.setActive(true);
        asset1.setAssetType(mockAssetType);

        AssetModel asset2 = new AssetModel();
        asset2.setName("Inactive Asset");
        asset2.setActive(false);
        asset2.setAssetType(mockAssetType);

        AssetModel asset3 = new AssetModel();
        asset3.setName("Other type of Asset");
        asset3.setActive(true);
        asset3.setAssetType(mock(AssetType.class));

        List<AssetModel> allAssets = List.of(asset1, asset2, asset3);

        when(assetRepository.findByAssetType(mockAssetType)).thenReturn(
                allAssets.stream()
                        .filter(asset -> asset.getAssetType() == mockAssetType)
                        .collect(Collectors.toList())
        );

        List<AssetResponseDTO> result = assetService.getActiveAssetsByAssetType(mockAssetType);

        assertEquals(1, result.size());
        assertEquals("Active Asset", result.get(0).getName());
    }

    @Test
    @DisplayName("Should return only active available assets")
    void testGetAvailableAssets_ReturnsOnlyActiveAssets() {
        AssetModel activeAsset1 = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Active Asset 1")
                .isActive(true)
                .build();
        AssetModel activeAsset2 = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Active Asset 2")
                .isActive(true)
                .build();
        AssetModel inactiveAsset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Inactive Asset")
                .isActive(false)
                .build();

        when(assetRepository.findByIsActiveTrue())
                .thenReturn(List.of(activeAsset1, activeAsset2));

        List<AssetResponseDTO> availableAssets = assetService.getAvailableAssets();

        assertNotNull(availableAssets);
        assertEquals(2, availableAssets.size());

        List<UUID> returnedIds = availableAssets.stream()
                .map(AssetResponseDTO::getId)
                .collect(Collectors.toList());

        assertTrue(returnedIds.contains(activeAsset1.getId()));
        assertTrue(returnedIds.contains(activeAsset2.getId()));
        assertFalse(returnedIds.contains(inactiveAsset.getId()));

        verify(assetRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Deve ativar um ativo válido com admin válido")
    void shouldActivateAssetWithValidAdmin() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        doNothing().when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());
        when(assetRepository.save(any(AssetModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponseDTO response = assetService.setIsActive(assetId, dto);

        assertThat(response.isActive()).isTrue();
        verify(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());
        verify(assetRepository).save(asset);
    }

    @Test
    @DisplayName("Deve desativar um ativo válido com admin válido")
    void shouldDeactivateAssetWithValidAdmin() {
        asset.setActive(true); // ativo inicialmente

        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("valid_code")
                .isActive(false)
                .build();

        doNothing().when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());
        when(assetRepository.save(any(AssetModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponseDTO response = assetService.setIsActive(assetId, dto);

        assertThat(response.isActive()).isFalse();
        verify(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());
        verify(assetRepository).save(asset);
    }

    @Test
    @DisplayName("Should throw exception when activating with incorrect email")
    void shouldThrowExceptionWithInvalidEmail() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("wrong@example.com")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("Unauthorized admin access: email or access code is incorrect"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("Unauthorized admin access: email or access code is incorrect");
    }

    @Test
    @DisplayName("Should throw exception with invalid access code and check message")
    void shouldThrowExceptionWithInvalidAccessCode() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("wrong_code")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("Unauthorized admin access: email or access code is incorrect"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertEquals("Unauthorized admin access: email or access code is incorrect", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when activating with incorrect email and access code")
    void shouldThrowExceptionWithInvalidEmailAndAccessCode() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("wrong@example.com")
                .adminAccessCode("wrong_code")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("Unauthorized admin access: email or access code is incorrect"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("Unauthorized admin access: email or access code is incorrect");
    }

    @Test
    @DisplayName("Should throw exception when activating with null email")
    void shouldThrowExceptionWithNullEmail() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail(null)
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be null"))
                .when(adminService).validateAdmin(isNull(), anyString());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("The 'adminEmail' cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when activating with blank email")
    void shouldThrowExceptionWithBlankEmail() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("   ")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be blank"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("The 'adminEmail' cannot be blank");
    }

    @Test
    @DisplayName("Should throw exception when activating with null access code")
    void shouldThrowExceptionWithNullAccessCode() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode(null)
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be null"))
                .when(adminService).validateAdmin(anyString(), isNull());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("The 'adminAccessCode' cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when activating with blank access code")
    void shouldThrowExceptionWithBlankAccessCode() {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("   ")
                .isActive(true)
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be blank"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(
                UnauthorizedUserAccessException.class,
                () -> assetService.setIsActive(assetId, dto)
        );

        assertThat(exception.getMessage()).isEqualTo("The 'adminAccessCode' cannot be blank");
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException if asset does not exist")
    void shouldThrowWhenAssetNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(assetRepository.findById(unknownId)).thenReturn(Optional.empty());

        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("valid_code")
                .isActive(true)
                .build();

        assertThrows(AssetNotFoundException.class, () -> assetService.setIsActive(unknownId, dto));

        verify(adminService, never()).validateAdmin(anyString(), anyString());
        verify(assetRepository, never()).save(any());
    }

}
