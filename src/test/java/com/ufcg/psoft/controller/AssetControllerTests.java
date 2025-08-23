package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.asset.AssetActivationPatchRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetPostRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetQuotationUpdateDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Assets controller tests")
class AssetControllerTests {

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
    private static final String ASSET_CRUD_URL = "/assets";
    private static final String QUOTATION_ENDPOINT = "/quotation";
    private static final String ACTIVATION_ENDPOINT = "/activation";
    private static final String AVAILABLE_ENDPOINT = "/available";
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
                .isActive(false)
                .assetType(type)
                .description("Default asset for tests")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();
    }

    @Test
    @DisplayName("Should create a new asset successfully")
    void testShouldCreateNewAssetSuccessfully() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("New Test Asset")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .description("A brand new asset for testing purposes.")
                .isActive(true)
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Test Asset"))
                .andExpect(jsonPath("$.quotation").value(200.0))
                .andExpect(jsonPath("$.quotaQuantity").value(500.0))
                .andExpect(jsonPath("$.assetType.name").value("STOCK"))
                .andExpect(jsonPath("$.description").value("A brand new asset for testing purposes."))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("Should delete an asset successfully")
    void testShouldDeleteAssetSuccessfully() throws Exception {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(delete(ASSET_BASE_URL + "/" + assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should return 404 if asset to delete not found")
    void testShouldReturnNotFoundIfAssetToDeleteDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(delete(ASSET_BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Asset not found with ID " + nonExistentId));
    }

    @Test
    @DisplayName("Should return 401 if admin credentials are invalid for delete")
    void testShouldReturnUnauthorizedIfAdminInvalidForDelete() throws Exception {
        AssetDeleteRequestDTO dto = AssetDeleteRequestDTO.builder()
                .adminEmail("invalid@example.com")
                .adminAccessCode("wrong-code")
                .build();

        mockMvc.perform(delete(ASSET_BASE_URL + "/" + assetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized admin access: email or access code is incorrect"));
    }

    @Test
    @DisplayName("Should return bad request when asset name is null")
    void testShouldReturnBadRequestWhenAssetNameIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name(null) // Violates @NotNull
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Some valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Required asset name")));
    }

    @Test
    @DisplayName("Should return bad request when asset name is blank")
    void testShouldReturnBadRequestWhenAssetNameIsBlankForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("") // Violates @NotBlank
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Some valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Required asset name")));
    }

    @Test
    @DisplayName("Should return bad request when asset type is null")
    void testShouldReturnBadRequestWhenAssetTypeIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(null) // Violates @NotNull
                .description("Some valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Required asset type")));
    }

    @Test
    @DisplayName("Should return bad request when description is null")
    void testShouldReturnBadRequestWhenDescriptionIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description(null) // Violates @NotNull
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Required asset description")));
    }

    @Test
    @DisplayName("Should return bad request when description is blank")
    void testShouldReturnBadRequestWhenDescriptionIsBlankForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("") // Violates @NotBlank
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Required asset description")));
    }

    @Test
    @DisplayName("Should return bad request when isActive is null")
    void testShouldReturnBadRequestWhenIsActiveIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(200.0)
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(null) // Violates @NotNull
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("must not be null")));
    }

    @Test
    @DisplayName("Should return bad request when quotation is null")
    void testShouldReturnBadRequestWhenQuotationIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(null) // Violates @NotNull
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("must not be null")));
    }

    @Test
    @DisplayName("Should return bad request when quotation is negative")
    void testShouldReturnBadRequestWhenQuotationIsNegativeForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(-10.0) // Violates @PositiveOrZero
                .quotaQuantity(500.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Asset quotation must be positive or zero!")));
    }

    @Test
    @DisplayName("Should return bad request when quota quantity is null")
    void testShouldReturnBadRequestWhenQuotaQuantityIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(100.0)
                .quotaQuantity(null) // Violates @NotNull
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("must not be null")));
    }

    @Test
    @DisplayName("Should return bad request when quota quantity is negative")
    void testShouldReturnBadRequestWhenQuotaQuantityIsNegativeForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(100.0)
                .quotaQuantity(-5.0) // Violates @PositiveOrZero
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("Asset quotation quantity must be positive or zero!")));
    }

    @Test
    @DisplayName("Should return bad request when admin email is null")
    void testShouldReturnBadRequestWhenAdminEmailIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail(null) // Violates @NotNull
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'admin_email' field cannot be null")));
    }

    @Test
    @DisplayName("Should return bad request when admin access code is null")
    void testShouldReturnBadRequestWhenAdminAccessCodeIsNullForPost() throws Exception {
        AssetPostRequestDTO dto = AssetPostRequestDTO.builder()
                .name("Valid Asset Name")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .assetType(AssetTypeEnum.STOCK)
                .description("Valid description.")
                .isActive(true)
                .adminEmail("admin@example.com")
                .adminAccessCode(null) // Violates @NotNull
                .build();

        mockMvc.perform(post(ASSET_CRUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'admin_access_code_email' cannot be null")));
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
                .andExpect(jsonPath("$.message").value("Asset not found with ID " + nonExistentId));
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
        String malformedJson = "{ \"quotation\": 105.0"; // missing closing

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
                .andExpect(jsonPath("$.quotaQuantity").value(1000.0));
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
                .andExpect(jsonPath("$.message").value("Unauthorized admin access: email or access code is incorrect"
                ));
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

    @Test
    @DisplayName("Should activate an asset successfully")
    void testActivateAssetSuccessfully() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.assetId.toString()))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("Should deactivate an asset successfully")
    void testDeactivateAssetSuccessfully() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .isActive(false)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.assetId.toString()))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("Should return 404 Not Found when deactivating asset with non-existent ID")
    void testDeactivateAssetWithNonExistentId() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .isActive(false)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + nonExistentId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Asset not found with ID " + nonExistentId));
    }

    @Test
    @DisplayName("Should fail when isActive is null")
    void testActivateAsset_MissingIsActive() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .isActive(null)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'isActive' cannot be null")));
    }

    @Test
    @DisplayName("Should fail when adminEmail is invalid")
    void testActivateAsset_InvalidAdminEmail() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("fake_admin@example.com")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized admin access: email or access code is incorrect"));
    }

    @Test
    @DisplayName("Should fail when adminEmail is null")
    void testActivateAsset_NullAdminEmail() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail(null)
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'adminEmail' cannot be null")));
    }

    @Test
    @DisplayName("Should fail when adminEmail is blank")
    void testActivateAsset_BlankAdminEmail() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("")
                .adminAccessCode("123456")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'adminEmail' cannot be blank")));
    }

    @Test
    @DisplayName("Should fail when the accessCode is invalid")
    void testActivateAsset_InvalidAdminAccessCode() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("1234567")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized admin access: email or access code is incorrect"));
    }

    @Test
    @DisplayName("Should fail when the accessCode is null")
    void testActivateAsset_NullAdminAccessCode() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode(null)
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'adminAccessCode' cannot be null")));
    }

    @Test
    @DisplayName("Should fail when the accessCode is blank")
    void testActivateAsset_BlankAdminAccessCode() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value(hasItem("The 'adminAccessCode' cannot be blank")));
    }

    @Test
    @DisplayName("Should fail when both adminEmail and accessCode are invalid")
    void testActivateAsset_InvalidAdminEmailAndAccessCode() throws Exception {
        AssetActivationPatchRequestDTO dto = AssetActivationPatchRequestDTO.builder()
                .adminEmail("fake_admin@example.com")
                .adminAccessCode("1234567")
                .isActive(true)
                .build();

        mockMvc.perform(patch(ASSET_BASE_URL + this.assetId + ACTIVATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized admin access: email or access code is incorrect"));
    }

    @Test
    @DisplayName("Should return only active assets")
    void testGetAvailableAssets_ReturnsOnlyActiveAssets() throws Exception {
        AssetModel activeAsset = AssetModel.builder()
                .name("Tesla Stock")
                .assetType(stockType)
                .description("Ações da Tesla")
                .quotation(850.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .build();

        AssetModel inactiveAsset = AssetModel.builder()
                .name("Old Crypto")
                .assetType(cryptoType)
                .description("Deactivated")
                .quotation(10.0)
                .quotaQuantity(500.0)
                .isActive(false)
                .build();

        assetRepository.saveAll(List.of(activeAsset, inactiveAsset));

        mockMvc.perform(get(ASSET_BASE_URL + AVAILABLE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Tesla Stock"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }
}
