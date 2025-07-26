package com.ufcg.psoft.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ufcg.psoft.commerce.dto.asset.AssetQuotationUpdateDTO;
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
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

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
}
