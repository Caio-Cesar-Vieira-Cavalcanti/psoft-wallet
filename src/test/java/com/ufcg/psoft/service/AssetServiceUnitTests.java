package com.ufcg.psoft.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ufcg.psoft.commerce.dto.asset.*;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.*;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.asset.AssetServiceImpl;

import com.ufcg.psoft.commerce.service.observer.EventManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AssetServiceUnitTests {

    private AssetRepository assetRepository;
    private AssetTypeRepository assetTypeRepository;
    private AdminService adminService;
    private AssetService assetService;
    private ModelMapper modelMapper;
    private EventManager assetEventManager;

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
        assetEventManager = mock(EventManager.class);

        assetService = new AssetServiceImpl();
        modelMapper = new ModelMapper();

        ReflectionTestUtils.setField(assetService, "assetRepository", assetRepository);
        ReflectionTestUtils.setField(assetService, "assetTypeRepository", assetTypeRepository);
        ReflectionTestUtils.setField(assetService, "adminService", adminService);
        ReflectionTestUtils.setField(assetService, "modelMapper", modelMapper);
        ReflectionTestUtils.setField(assetService, "assetEventManager", assetEventManager);

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

    @AfterEach
    void tearDown() {
        assetRepository.deleteAll();
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when creating asset with null admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenCreatingAssetWithNullAdminEmail() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Asset")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .adminEmail(null) // Null email
                .adminAccessCode("123456")
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be null"))
                .when(adminService).validateAdmin(isNull(), anyString());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.create(dto)
        );

        assertEquals("The 'adminEmail' cannot be null", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when creating asset with blank admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenCreatingAssetWithBlankAdminEmail() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Asset")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .adminEmail("   ") // Blank email
                .adminAccessCode("123456")
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be blank"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.create(dto)
        );

        assertEquals("The 'adminEmail' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when creating asset with null admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenCreatingAssetWithNullAdminAccessCode() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Asset")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .adminEmail("admin@example.com")
                .adminAccessCode(null) // Null access code
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be null"))
                .when(adminService).validateAdmin(anyString(), isNull());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.create(dto)
        );

        assertEquals("The 'adminAccessCode' cannot be null", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when creating asset with blank admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenCreatingAssetWithBlankAdminAccessCode() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Asset")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .adminEmail("admin@example.com")
                .adminAccessCode("   ") // Blank access code
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be blank"))
                .when(adminService).validateAdmin(anyString(), eq("   "));

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.create(dto)
        );

        assertEquals("The 'adminAccessCode' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw AssetTypeNotFoundException when creating asset with an asset type not found in repository")
    void testShouldThrowAssetTypeNotFoundExceptionWhenCreatingAssetWithUnknownAssetType() {
        AssetTypeEnum assetTypeToTest = AssetTypeEnum.CRYPTO;

        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Asset")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(assetTypeToTest)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        when(assetTypeRepository.findByName(assetTypeToTest.name())).thenReturn(Optional.empty());

        AssetTypeNotFoundException exception = assertThrows(AssetTypeNotFoundException.class, () ->
                assetService.create(dto)
        );

        assertEquals("Asset type:" + assetTypeToTest.name() + " not found!", exception.getMessage());
        verify(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when deleting asset with null admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenDeletingAssetWithNullAdminEmail() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail(null) // Null email
                .adminAccessCode("123456")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset)); // Asset exists

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be null"))
                .when(adminService).validateAdmin(isNull(), anyString());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.delete(assetId, dto)
        );

        assertEquals("The 'adminEmail' cannot be null", exception.getMessage());
        verify(assetRepository, never()).delete(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when deleting asset with blank admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenDeletingAssetWithBlankAdminEmail() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("   ") // Blank email
                .adminAccessCode("123456")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be blank"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.delete(assetId, dto)
        );

        assertEquals("The 'adminEmail' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).delete(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when deleting asset with null admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenDeletingAssetWithNullAdminAccessCode() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode(null) // Null access code
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be null"))
                .when(adminService).validateAdmin(anyString(), isNull());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.delete(assetId, dto)
        );

        assertEquals("The 'adminAccessCode' cannot be null", exception.getMessage());
        verify(assetRepository, never()).delete(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when deleting asset with blank admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenDeletingAssetWithBlankAdminAccessCode() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("   ") // Blank access code
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be blank"))
                .when(adminService).validateAdmin(anyString(), eq("   "));

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.delete(assetId, dto)
        );

        assertEquals("The 'adminAccessCode' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).delete(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw AssetReferencedInPurchaseException when deleting an asset referenced by a purchase")
    void testShouldThrowAssetReferencedInPurchaseExceptionWhenDeletingReferencedAsset() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        doThrow(DataIntegrityViolationException.class).when(assetRepository).delete(asset);

        assertThrows(AssetReferencedInPurchaseException.class, () ->
                assetService.delete(assetId, dto)
        );

        verify(assetRepository, times(1)).delete(asset);
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when updating quotation with null admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenUpdatingQuotationWithNullAdminEmail() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail(null) // Null email
                .adminAccessCode("secret")
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be null"))
                .when(adminService).validateAdmin(isNull(), anyString());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        assertEquals("The 'adminEmail' cannot be null", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when updating quotation with blank admin email")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenUpdatingQuotationWithBlankAdminEmail() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail("   ") // Blank email
                .adminAccessCode("secret")
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminEmail' cannot be blank"))
                .when(adminService).validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        assertEquals("The 'adminEmail' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when updating quotation with null admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenUpdatingQuotationWithNullAdminAccessCode() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail("admin@example.com")
                .adminAccessCode(null) // Null access code
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be null"))
                .when(adminService).validateAdmin(anyString(), isNull());

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        assertEquals("The 'adminAccessCode' cannot be null", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when updating quotation with blank admin access code")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenUpdatingQuotationWithBlankAdminAccessCode() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(110.0)
                .adminEmail("admin@example.com")
                .adminAccessCode("   ") // Blank access code
                .build();

        doThrow(new UnauthorizedUserAccessException("The 'adminAccessCode' cannot be blank"))
                .when(adminService).validateAdmin(anyString(), eq("   "));

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () ->
                assetService.updateQuotation(assetId, dto)
        );

        assertEquals("The 'adminAccessCode' cannot be blank", exception.getMessage());
        verify(assetRepository, never()).save(any(AssetModel.class));
    }

    @Test
    @DisplayName("Should update the quotation successfully")
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
    @DisplayName("Should throw exception because the given asset id doesn't exist")
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
    @DisplayName("Should throw exception because the quotation variation is invalid")
    void testUpdateQuotation_ThrowsInvalidQuotationVariationException() {
        AssetQuotationUpdateDTO dto = AssetQuotationUpdateDTO.builder()
                .quotation(100.5)
                .adminEmail("admin@example.com")
                .adminAccessCode("secret")
                .build();

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    @DisplayName("Should throw exception because the asset type is invalid")
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
    @DisplayName("Should increase the quotation successfully")
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
    @DisplayName("Should decrease the quotation successfully")
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
    @DisplayName("Should throw exception because the positive variation is invalid")
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
    @DisplayName("Should throw exception because the negative variation is invalid")
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
    @DisplayName("Should throw exception because the request isn't from the admin")
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
    @DisplayName("Should throw exception because the request isn't from the admin even if the quotation is valid")
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
    @DisplayName("Should create a asset successfully")
    void testCreateAsset_Success() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Bitcoin")
                .quotation(50000000.0)
                .quotaQuantity(100000.0)
                .assetType(AssetTypeEnum.CRYPTO)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        AssetType mockType = mockStockType();
        when(assetTypeRepository.findByName("CRYPTO")).thenReturn(Optional.of(mockType));
        when(assetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponseDTO response = assetService.create(dto);

        verify(assetRepository).save(any(AssetModel.class));
        assertEquals("Bitcoin", response.getName());
        assertEquals(50000000.0, response.getQuotation());
    }

    @Test
    @DisplayName("Should throw exception because the request isn't from the admin")
    void testCreateAsset_UnauthorizedAdmin() {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Bitcoin")
                .quotation(50000000.0)
                .quotaQuantity(100000.0)
                .assetType(AssetTypeEnum.CRYPTO)
                .adminEmail("notadmin@example.com")
                .adminAccessCode("654321")
                .build();

        doThrow(new UnauthorizedUserAccessException("Unauthorized")).when(adminService)
                .validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        assertThrows(UnauthorizedUserAccessException.class, () -> assetService.create(dto));
    }

    @Test
    @DisplayName("Should delete a asset successfully")
    void testDeleteAsset_Success() {
        AssetPostRequestDTO postDto = AssetPostRequestDTO.builder()
                .name("Bitcoin")
                .quotation(50000000.0)
                .quotaQuantity(100000.0)
                .assetType(AssetTypeEnum.CRYPTO)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        AssetType mockType = mockStockType();
        when(assetTypeRepository.findByName("CRYPTO")).thenReturn(Optional.of(mockType));
        when(assetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponseDTO created = assetService.create(postDto);

        verify(assetRepository).save(any(AssetModel.class));
        AssetModel persistedAsset = new AssetModel();
        persistedAsset.setId(assetId);
        persistedAsset.setName(created.getName());
        persistedAsset.setQuotation(created.getQuotation());
        persistedAsset.setQuotaQuantity(postDto.getQuotaQuantity());
        persistedAsset.setAssetType(mockType);
        persistedAsset.setActive(true);

        AssetDeleteRequestDTO deleteDto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(persistedAsset));

        assertDoesNotThrow(() -> assetService.delete(assetId, deleteDto));
        verify(assetRepository).delete(persistedAsset);
    }

    @Test
    @DisplayName("Should throw exception because the request isn't from the admin")
    void testDeleteAsset_UnauthorizedAdmin() {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("notadmin@example.com")
                .adminAccessCode("654321")
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        doThrow(new UnauthorizedUserAccessException("Unauthorized")).when(adminService)
                .validateAdmin(dto.getAdminEmail(), dto.getAdminAccessCode());

        assertThrows(UnauthorizedUserAccessException.class, () -> assetService.delete(assetId, dto));
    }

    @Test
    @DisplayName("Should throw exception because the id is invalid")
    void testDeleteAsset_InvalidId() {
        UUID invalidId = UUID.randomUUID();

        when(assetRepository.findById(invalidId)).thenReturn(Optional.empty());

        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        assertThrows(AssetNotFoundException.class, () -> assetService.delete(invalidId, dto));
    }

    @Test
    @DisplayName("Should get all assets sucessfully")
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
    @DisplayName("Should get the asset using his id successfully")
    void testGetAssetById_Sucess() {
        AssetResponseDTO response = assetService.getAssetById(assetId);

        assertNotNull(response);
        assertEquals(asset.getId(), response.getId());
        assertEquals(asset.getName(), response.getName());
    }

    @Test
    @DisplayName("Should throw exception because the id is invalid")
    void testGetAssetById_WithInvalidId() {
        UUID invalidId = UUID.randomUUID();
        when(assetRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(AssetNotFoundException.class, () ->
                assetService.getAssetById(invalidId));
    }

    @Test
    @DisplayName("Should get all active assets from a specific type successfully")
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
                        .filter(mockAsset -> mockAsset.getAssetType() == mockAssetType)
                        .toList()
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
                .toList();

        assertTrue(returnedIds.contains(activeAsset1.getId()));
        assertTrue(returnedIds.contains(activeAsset2.getId()));
        assertFalse(returnedIds.contains(inactiveAsset.getId()));

        verify(assetRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Must activate a valid asset with valid admin")
    void testShouldActivateAssetWithValidAdmin() {
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
    @DisplayName("Must deactivate a valid asset with valid admin")
    void testShouldDeactivateAssetWithValidAdmin() {
        asset.setActive(true);

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
    void testShouldThrowExceptionWithInvalidEmail() {
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
    void testShouldThrowExceptionWithInvalidAccessCode() {
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
    void testShouldThrowExceptionWithInvalidEmailAndAccessCode() {
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
    void testShouldThrowExceptionWithNullEmail() {
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
    void testShouldThrowExceptionWithBlankEmail() {
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
    void testShouldThrowExceptionWithNullAccessCode() {
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
    void testShouldThrowExceptionWithBlankAccessCode() {
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
    void testShouldThrowWhenAssetNotFound() {
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
