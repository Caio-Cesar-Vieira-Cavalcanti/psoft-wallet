package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;

import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de asset types")
class AssetTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/asset-types";

    @Test
    @DisplayName("Should return all asset types")
    void testGetAllAssetTypes() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.name=='STOCK')]").exists())
                .andExpect(jsonPath("$[?(@.name=='CRYPTO')]").exists())
                .andExpect(jsonPath("$[?(@.name=='TREASURY_BOUNDS')]").exists());
    }

    @Test
    @Transactional
    @DisplayName("Should return empty list when no asset types exist")
    void testGetAllAssetTypesEmpty() throws Exception {
        assetTypeRepository.deleteAll();

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
