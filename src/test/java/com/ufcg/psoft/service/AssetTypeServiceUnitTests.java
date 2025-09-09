package com.ufcg.psoft.service;


import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.service.asset.AssetTypeService;
import com.ufcg.psoft.commerce.service.asset.AssetTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("AssetType Service Unit Tests")
class AssetTypeServiceUnitTests {

    private AssetTypeRepository assetTypeRepository;
    private ModelMapper modelMapper;
    private AssetTypeService assetTypeService;

    @BeforeEach
    void setUp() {
        assetTypeRepository = mock(AssetTypeRepository.class);
        modelMapper = new ModelMapper();
        assetTypeService = new AssetTypeServiceImpl();

        ReflectionTestUtils.setField(assetTypeService, "assetTypeRepository", assetTypeRepository);
        ReflectionTestUtils.setField(assetTypeService, "modelMapper", modelMapper);
    }

    @Test
    @DisplayName("Should return all asset types")
    void shouldReturnAllAssetTypes() {
        AssetType stockType = new Stock();
        stockType.setId(1L);
        stockType.setName("STOCK");

        AssetType cryptoType = new Crypto();
        cryptoType.setId(2L);
        cryptoType.setName("CRYPTO");

        List<AssetType> mockAssetTypes = Arrays.asList(stockType, cryptoType);

        when(assetTypeRepository.findAll()).thenReturn(mockAssetTypes);

        List<AssetTypeResponseDTO> result = assetTypeService.getAllAssetTypes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("STOCK", result.get(0).getName());
        assertEquals("CRYPTO", result.get(1).getName());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(assetTypeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return an empty list if no asset types exist")
    void shouldReturnEmptyListWhenNoAssetTypesExist() {
        when(assetTypeRepository.findAll()).thenReturn(Collections.emptyList());

        List<AssetTypeResponseDTO> result = assetTypeService.getAllAssetTypes();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(assetTypeRepository, times(1)).findAll();
    }
}
