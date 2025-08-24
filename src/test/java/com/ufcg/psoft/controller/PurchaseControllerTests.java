package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Purchase controller tests")
class PurchaseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID purchaseId;
    private UUID walletId;
    private UUID assetId;
    private AssetType stockType;

    private static final String PURCHASE_BASE_URL = "/purchases/";
    private static final String CONFIRMATION_ENDPOINT = "/confirmation";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_ACCESS_CODE = "123456";
    private static final String INVALID_JSON = "{ invalid json }";

    @BeforeEach
    void setup() {
        stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));

        WalletModel wallet = WalletModel.builder()
                .budget(10000.0)
                .build();
        walletId = walletRepository.save(wallet).getId();

        AssetModel asset = AssetModel.builder()
                .name("Test Stock")
                .description("Test stock asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .assetType(stockType)
                .build();
        assetId = assetRepository.save(asset).getId();

        PurchaseModel purchase = PurchaseModel.builder()
                .wallet(wallet)
                .asset(asset)
                .quantity(10)
                .acquisitionPrice(100.0)
                .date(LocalDate.now())
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build();
        purchaseId = purchaseRepository.save(purchase).getId();
    }

    @AfterEach
    void tearDown() {
        purchaseRepository.deleteAll();
        walletRepository.deleteAll();
        assetRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    @DisplayName("Should confirm purchase availability successfully")
    void testShouldConfirmPurchaseAvailabilitySuccessfully() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(purchaseId.toString())))
                .andExpect(jsonPath("$.walletId", is(walletId.toString())))
                .andExpect(jsonPath("$.assetId", is(assetId.toString())))
                .andExpect(jsonPath("$.quantity", is(10.0)))
                .andExpect(jsonPath("$.state", is(PurchaseStateEnum.AVAILABLE.name())))
                .andExpect(jsonPath("$.date", notNullValue()));
    }

    @Test
    @DisplayName("Should return 404 when purchase not found")
    void testShouldReturn404WhenPurchaseNotFound() throws Exception {
        UUID nonExistentPurchaseId = UUID.randomUUID();
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + nonExistentPurchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Purchase not found")));
    }

    @Test
    @DisplayName("Should return 400 when admin email is null")
    void testShouldReturn400WhenAdminEmailIsNull() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(null)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation errors found")))
                .andExpect(jsonPath("$.errors", hasItem("The 'adminEmail' cannot be null")));
    }

    @Test
    @DisplayName("Should return 400 when admin email is blank")
    void testShouldReturn400WhenAdminEmailIsBlank() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail("")
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation errors found")))
                .andExpect(jsonPath("$.errors", hasItem("The 'adminEmail' cannot be blank")));
    }

    @Test
    @DisplayName("Should return 400 when admin access code is null")
    void testShouldReturn400WhenAdminAccessCodeIsNull() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(null)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation errors found")))
                .andExpect(jsonPath("$.errors", hasItem("The 'adminAccessCode' cannot be null")));
    }

    @Test
    @DisplayName("Should return 400 when admin access code is blank")
    void testShouldReturn400WhenAdminAccessCodeIsBlank() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode("")
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation errors found")))
                .andExpect(jsonPath("$.errors", hasItem("The 'adminAccessCode' cannot be blank")));
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid JSON")
    void testShouldReturn400WhenRequestBodyIsInvalidJson() throws Exception {
        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INVALID_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Malformed JSON request.")));
    }

    @Test
    @DisplayName("Should return 400 when purchase ID is invalid UUID")
    void testShouldReturn400WhenPurchaseIdIsInvalidUuid() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + "invalid-uuid" + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        // Não validamos a mensagem porque o Spring retorna 400 sem corpo JSON para UUID inválido
    }

    @Test
    @DisplayName("Should verify asset availability before confirming purchase")
    void testShouldVerifyAssetAvailabilityBeforeConfirmingPurchaseUSTest() throws Exception {
        AssetModel assetToUpdate = assetRepository.findById(assetId).orElseThrow();
        assetToUpdate.setActive(false);
        assetRepository.save(assetToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Asset is inactive!")));
    }

    @Test
    @DisplayName("Should verify liquidity sufficiency before confirming purchase")
    void testShouldVerifyLiquiditySufficiencyBeforeConfirmingPurchaseUSTest() throws Exception {
        AssetModel assetToUpdate = assetRepository.findById(assetId).orElseThrow();
        assetToUpdate.setQuotaQuantity(5.0); // Less than purchase quantity (10)
        assetRepository.save(assetToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Test Stock")))
                .andExpect(jsonPath("$.message", containsString("10")))
                .andExpect(jsonPath("$.message", containsString("5.0")));
    }

    @Test
    @DisplayName("Should verify admin authorization before confirming availability")
    void testShouldVerifyAdminAuthorizationBeforeConfirmingAvailabilityUSTest() throws Exception {
        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail("wrong@email.com")
                .adminAccessCode("wrong123")
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Unauthorized admin access: email or access code is incorrect")));
    }

    @Test
    @DisplayName("Should verify complete workflow: REQUESTED -> AVAILABLE")
    void testShouldVerifyCompleteWorkflowRequestedToAvailableUSTest() throws Exception {
        PurchaseModel purchaseToUpdate = purchaseRepository.findById(purchaseId).orElseThrow();
        purchaseToUpdate.setStateEnum(PurchaseStateEnum.REQUESTED);
        purchaseRepository.save(purchaseToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is(PurchaseStateEnum.AVAILABLE.name())));
    }

    @Test
    @DisplayName("Should verify purchase state transition validation")
    void testShouldVerifyPurchaseStateTransitionValidationUSTest() throws Exception {
        PurchaseModel purchaseToUpdate = purchaseRepository.findById(purchaseId).orElseThrow();
        purchaseToUpdate.setStateEnum(PurchaseStateEnum.AVAILABLE);
        purchaseRepository.save(purchaseToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("PURCHASED")));
    }

    @Test
    @DisplayName("Should verify asset and purchase quantity validation - US Test")
    void testShouldVerifyAssetAndPurchaseQuantityValidationUSTest() throws Exception {
        AssetModel assetToUpdate = assetRepository.findById(assetId).orElseThrow();
        assetToUpdate.setQuotaQuantity(10.0); // Exactly the same as purchase quantity
        assetRepository.save(assetToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is(PurchaseStateEnum.AVAILABLE.name())));
    }

    @Test
    @DisplayName("Should verify purchase confirmation with different asset quantities")
    void testShouldVerifyPurchaseConfirmationWithDifferentAssetQuantitiesUSTest() throws Exception {
        // Arrange - Set higher quantity than needed
        AssetModel assetToUpdate = assetRepository.findById(assetId).orElseThrow();
        assetToUpdate.setQuotaQuantity(50.0); // More than purchase quantity (10)
        assetRepository.save(assetToUpdate);

        PurchaseConfirmationRequestDTO dto = PurchaseConfirmationRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + purchaseId + CONFIRMATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is(PurchaseStateEnum.AVAILABLE.name())));
    }
}
