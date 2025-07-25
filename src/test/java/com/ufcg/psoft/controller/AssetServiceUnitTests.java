package com.ufcg.psoft.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ufcg.psoft.commerce.dto.asset.AssetPatchRequestDTO;
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
    private AssetService assetService;
    private AdminService adminService;
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
    void setup() {
        assetRepository = mock(AssetRepository.class);
        assetTypeRepository = mock(AssetTypeRepository.class);
        modelMapper = new ModelMapper();
        adminService = mock(AdminService.class);

        assetService = new AssetServiceImpl();

        ReflectionTestUtils.setField(assetService, "assetRepository", assetRepository);
        ReflectionTestUtils.setField(assetService, "assetTypeRepository", assetTypeRepository);
        ReflectionTestUtils.setField(assetService, "modelMapper", modelMapper);
        ReflectionTestUtils.setField(assetService, "adminService", adminService);

        assetId = UUID.randomUUID();
        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .assetType(mockStockType())
                .build();

        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
    }

    @Test
    void testUpdateQuotation_Success() {
        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(105.0);

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(105.0, response.getQuotation());
        verify(assetRepository).save(asset);
    }

    @Test
    void testUpdateQuotation_ThrowsAssetNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(assetRepository.findById(unknownId)).thenReturn(Optional.empty());

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(110.0);

        assertThrows(AssetNotFoundException.class, () -> assetService.updateQuotation(unknownId, dto));
    }

    @Test
    void testUpdateQuotation_ThrowsInvalidQuotationVariationException() {
        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(100.5);

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

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(120.0);

        assertThrows(InvalidAssetTypeException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_MinimumValidVariation_ShouldUpdate() {
        double current = 100.0;
        double minVariation = 0.01;

        double newQuotation = current * (1 + minVariation);

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(newQuotation);

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(newQuotation, response.getQuotation());
    }

    @Test
    void testUpdateQuotation_MinimumValidNegativeVariation_ShouldUpdate() {
        double current = 100.0;
        double minVariation = 0.01;

        double newQuotation = current * (1 - minVariation);

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(newQuotation);

        when(assetRepository.save(any())).thenReturn(asset);

        var response = assetService.updateQuotation(assetId, dto);

        assertEquals(newQuotation, response.getQuotation());
    }

    @Test
    void testUpdateQuotation_InvalidPositiveVariation_ShouldThrowException() {
        double current = 100.0;
        double invalidVariation = 0.005;

        double newQuotation = current * (1 + invalidVariation);

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(newQuotation);

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }

    @Test
    void testUpdateQuotation_InvalidNegativeVariation_ShouldThrowException() {
        double current = 100.0;
        double invalidVariation = 0.005;

        double newQuotation = current * (1 - invalidVariation);

        AssetPatchRequestDTO dto = new AssetPatchRequestDTO();
        dto.setQuotation(newQuotation);

        assertThrows(InvalidQuotationVariationException.class, () -> assetService.updateQuotation(assetId, dto));
    }
}
