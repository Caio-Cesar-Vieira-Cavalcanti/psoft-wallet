package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.asset.AssetQuotationUpdateDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Assets controller tests")
public class AssetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID assetId;
    private AssetType stockType;
    private AssetType treasuryBoundsType;
    private AssetType cryptoType;

    private static final String ASSET_BASE_URL = "/assets/";
    private static final String QUOTATION_ENDPOINT = "/quotation";
    private static final String STOCK_ASSET_TYPE_NAME = "STOCK";
    private static final String TREASURY_BOUNDS_ASSET_TYPE_NAME = "TREASURY_BOUNDS";


    @BeforeEach
    void setup() {
        stockType = assetTypeRepository.findByName(STOCK_ASSET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));

        treasuryBoundsType = assetTypeRepository.findByName(TREASURY_BOUNDS_ASSET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("No TREASURY_BOUNDS asset found. Please ensure it's pre-populated for tests."));

        cryptoType = assetTypeRepository.findByName("CRYPTO")
                .orElseThrow(() -> new RuntimeException("No CRYPTO asset found. Please ensure it's pre-populated for tests."));

        AssetModel asset = createDefaultAsset(stockType);
        assetId = assetRepository.save(asset).getId();
    }

    @AfterEach
    void tearDown() {
        assetRepository.deleteAll();
    }

    private AssetModel createDefaultAsset(AssetType type) {
        if (type == null) {
            throw new IllegalArgumentException("AssetType cannot be null when creating an AssetModel.");
        }
        return AssetModel.builder()
                .name("Default Asset Test")
                .assetType(type)
                .description("Default asset for tests")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();
    }

    @Test
    @DisplayName("Successfully update quotation for Stock asset type")
    void testUpdateQuotation_Success() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(105.0);

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotation").value(105.0))
                .andExpect(jsonPath("$.name").value("Default Asset Test"));
    }

    @Test
    @DisplayName("Successfully update quotation for Crypto asset type")
    void testUpdateQuotation_SuccessCrypto() throws Exception {
        AssetModel cryptoAsset = createDefaultAsset(cryptoType);
        cryptoAsset = assetRepository.save(cryptoAsset);

        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(150.0);

        mockMvc.perform(patch(ASSET_BASE_URL + cryptoAsset.getId() + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotation").value(150.0));
    }

    @Test
    @DisplayName("Fails with variation below minimum (0.1%)")
    void testUpdateQuotation_FailsIfVariationTooSmall() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(100.1);

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The quotation variation should be at least 1% between updates."));
    }

    @Test
    @DisplayName("Fails if asset type don't should change quotation")
    void testUpdateQuotation_InvalidType() throws Exception {
        AssetModel nonUpdatableAsset = createDefaultAsset(treasuryBoundsType);
        nonUpdatableAsset = assetRepository.save(nonUpdatableAsset);
        UUID nonUpdatableAssetId = nonUpdatableAsset.getId();

        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(120.0);

        mockMvc.perform(patch(ASSET_BASE_URL + nonUpdatableAssetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only Stock or Crypto assets can have the quotation updated."));
    }

    @Test
    @DisplayName("Fails if assetId not found")
    void testUpdateQuotation_AssetNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(110.0);

        mockMvc.perform(patch(ASSET_BASE_URL + nonExistentId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Asset not found!"));
    }

    @Test
    @DisplayName("Fails if quotation is null")
    void testUpdateQuotation_NullQuotation() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(null);

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Successfully update quotation with valid negative variation")
    void testUpdateQuotation_ValidNegativeVariation() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(98.0); // -2% variation from 100.0

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotation").value(98.0));
    }

    @Test
    @DisplayName("Fails on malformed JSON input")
    void testUpdateQuotation_MalformedJson() throws Exception {
        String malformedJson = "{ \"quotation\": 105.0"; // missing closing }

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch updates only quotation, leaving other fields intact")
    void testPatchPartialUpdate() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");
        dto.setQuotation(120.0);

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotation").value(120.0))
                .andExpect(jsonPath("$.quota_quantity").value(1000.0));
    }

    @Test
    @DisplayName("Fails if request body is empty")
    void testUpdateQuotation_EmptyBody() throws Exception {
        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Fails if admin credentials are invalid")
    void testUpdateQuotation_AdminUnauthorized() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setQuotation(110.0);
        dto.setAdminEmail("invalid@example.com");
        dto.setAdminAccessCode("wrong-code");

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized admin access"));
    }

    @Test
    @DisplayName("Successfully updates quotation with authorized admin")
    void testUpdateQuotation_AdminAuthorized() throws Exception {
        AssetQuotationUpdateDTO dto = new AssetQuotationUpdateDTO();
        dto.setQuotation(110.0);
        dto.setAdminEmail("admin@example.com");
        dto.setAdminAccessCode("123456");

        mockMvc.perform(patch(ASSET_BASE_URL + assetId + QUOTATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotation").value(110.0));
    }
}
