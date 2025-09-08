package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import com.ufcg.psoft.commerce.service.wallet.WalletService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Purchase controller tests")
class PurchaseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HoldingRepository holdingRepository;

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
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID purchaseId;
    private UUID walletId;
    private UUID assetId;
    private UUID clientId;
    private AssetType stockType;

    private static final String PURCHASE_BASE_URL = "/purchases/";
    private static final String PURCHASES_ENDPOINT = "/purchase";
    private static final String WALLET = "/wallet";
    private static final String CONFIRMATION_ENDPOINT = "/availability-confirmation";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_ACCESS_CODE = "123456";
    private static final String INVALID_JSON = "{ invalid json }";

    @BeforeEach
    void setup() {

        stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));

        WalletModel wallet = WalletModel.builder()
                .budget(10000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );

        clientId = clientRepository.save(client).getId();
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
        holdingRepository.deleteAll();
        clientRepository.deleteAll();
        walletRepository.deleteAll();
        assetRepository.deleteAll();
    }

    private ClientModel createClient(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode,
                                     AddressModel address, PlanTypeEnum planType, WalletModel wallet) {
        return new ClientModel(id, fullName, email, accessCode, address, planType, wallet);
    }

    private AssetModel createAndSaveAsset(AssetType type) {
        if (type == null) {
            throw new IllegalArgumentException("AssetType cannot be null when creating an AssetModel.");
        }
        AssetModel assetModel = AssetModel.builder()
                .name("Default Asset Test")
                .isActive(false)
                .assetType(type)
                .description("Default asset for tests")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetRepository.save(assetModel);

        return assetModel;
    }

    private PurchaseModel createPurchase(UUID id, AssetModel asset, double quantity, LocalDate date, WalletModel wallet) {
        PurchaseModel purchase = PurchaseModel.builder()
                .id(id)
                .asset(asset)
                .wallet(wallet)
                .quantity(quantity)
                .date(date)
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();

        purchase = purchaseRepository.save(purchase);
        createAndAddHoldingToWallet(purchase);

        return purchase;
    }

    @Transactional
    private HoldingModel createAndAddHoldingToWallet(PurchaseModel purchase) {
        HoldingModel existingHolding = walletService.findHoldingByAsset(purchase.getWallet(), purchase.getAsset());

        if (existingHolding != null) {
            existingHolding.increaseQuantityAfterPurchase(purchase.getQuantity());
            existingHolding.increaseAccumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice());
            return holdingRepository.save(existingHolding);
        } else {
            HoldingModel holding = HoldingModel.builder()
                    .asset(purchase.getAsset())
                    .wallet(purchase.getWallet())
                    .quantity(purchase.getQuantity())
                    .accumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice())
                    .build();

            holding = holdingRepository.save(holding);

            WalletModel wallet = walletRepository.findById(purchase.getWallet().getId()).orElseThrow();
            if (wallet.getHoldings() == null) {
                wallet.setHoldings(new HashMap<>());
            }
            wallet.getHoldings().put(holding.getId(), holding);
            walletRepository.save(wallet);

            return holding;
        }
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

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testGetPurchaseHistory_WhenAccessCodeIsInvalid() throws Exception {
        ClientPurchaseHistoryRequestDTO requestDTO = new ClientPurchaseHistoryRequestDTO();
        requestDTO.setAccessCode("invalid_code");

        mockMvc.perform(MockMvcRequestBuilders.get(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is missing")
    void testGetPurchaseHistory_Fail_MissingAccessCode() throws Exception {
        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                // no accessCode
                .build();

        mockMvc.perform(get(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is empty")
    void testGetPurchaseHistory_Fail_EmptyAccessCode() throws Exception {
        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                .accessCode("")
                .build();

        mockMvc.perform(get(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return wallet with empty purchase list")
    void testGetPurchaseHistorySuccessfully_NoPurchases() throws Exception {
        WalletModel newWallet = WalletModel.builder()
                .budget(10000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel newClient = createClient(
                UUID.randomUUID(),
                "Rafael Barreto",
                new EmailModel("rafael@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                newWallet
        );

        UUID newClientId = clientRepository.save(newClient).getId();
        walletRepository.save(newWallet);

        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(get(PURCHASE_BASE_URL + "/" + newClientId + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Transactional
    @DisplayName("Should return purchases for a client wallet")
    void testGetPurchaseHistorySuccessfully_WithPurchases() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Walber Araújo",
                new EmailModel("walber@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );
        client = clientRepository.save(client);

        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        PurchaseModel purchase1 = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1), client.getWallet());
        PurchaseModel purchase2 = createPurchase(UUID.randomUUID(), asset, 3.0, LocalDate.now().minusDays(2), client.getWallet());

        purchaseRepository.saveAll(List.of(purchase1, purchase2));

        mockMvc.perform(get(PURCHASE_BASE_URL + "/" + client.getId() + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].quantity").value(5.0))
                .andExpect(jsonPath("$[1].quantity").value(3.0));
    }

    @Test
    @Transactional
    void confirmationByClient_createsNewHolding_ShouldReturn200() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                null, // deixa null se o ID for @GeneratedValue
                "Test User",
                new EmailModel("testuser@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );
        client = clientRepository.save(client);

        PurchaseModel purchase = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1), wallet);
        purchase = purchaseRepository.save(purchase);

        PurchaseConfirmationByClientDTO requestDto = PurchaseConfirmationByClientDTO.builder()
                .accessCode(client.getAccessCode().getAccessCode())
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + client.getId() + WALLET + PURCHASES_ENDPOINT + "/" + purchase.getId() + "/" + "confirmation-by-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchase.getId().toString()))
                .andExpect(jsonPath("$.walletId").value(wallet.getId().toString()))
                .andExpect(jsonPath("$.assetId").value(asset.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(5.0))
                .andExpect(jsonPath("$.state").value("IN_WALLET"))
                .andExpect(jsonPath("$.date").value(purchase.getDate().toString()));

        WalletModel updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        assertEquals(500, updatedWallet.getBudget());

        assertEquals(1, updatedWallet.getHoldings().size());
    }

    @Test
    @Transactional
    void confirmationByClient_alreadyExistsAHolding_ShouldReturn200() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                null, // deixa null se o ID for @GeneratedValue
                "Test User",
                new EmailModel("testuser@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );
        client = clientRepository.save(client);

        PurchaseModel purchase1 = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1),wallet);
        purchase1 = purchaseRepository.save(purchase1);

        PurchaseModel purchase2 = createPurchase(UUID.randomUUID(), asset, 3.0, LocalDate.now().minusDays(1), wallet);
        purchase2 = purchaseRepository.save(purchase2);

        PurchaseConfirmationByClientDTO requestDto = PurchaseConfirmationByClientDTO.builder()
                .accessCode(client.getAccessCode().getAccessCode())
                .build();

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + client.getId() + WALLET + PURCHASES_ENDPOINT + "/" + purchase1.getId() + "/" + "confirmation-by-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + client.getId() + WALLET + PURCHASES_ENDPOINT + "/" + purchase2.getId() + "/" + "confirmation-by-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchase2.getId().toString()))
                .andExpect(jsonPath("$.walletId").value(wallet.getId().toString()))
                .andExpect(jsonPath("$.assetId").value(asset.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(3.0))
                .andExpect(jsonPath("$.state").value("IN_WALLET"))
                .andExpect(jsonPath("$.date").value(purchase2.getDate().toString()));

        WalletModel updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        assertEquals(200, updatedWallet.getBudget());

        assertEquals(1, updatedWallet.getHoldings().size());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
    void testGetPurchaseHistory_WhenClientIdIsInvalid() throws Exception {
        UUID randomClientId = UUID.randomUUID();

        ClientPurchaseHistoryRequestDTO requestDTO = new ClientPurchaseHistoryRequestDTO();
        requestDTO.setAccessCode("789032");

        mockMvc.perform(MockMvcRequestBuilders.get(PURCHASE_BASE_URL + "/" + randomClientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void purchaseRequestForAvailableAsset_NullClientId() throws Exception {
        UUID nullClientId = null;
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + nullClientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_InvalidClientId() throws Exception {
        UUID invalidClientId = UUID.randomUUID();
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + invalidClientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void purchaseRequestForAvailableAsset_NullAssetId() throws Exception {
        UUID nullAssetId = null;
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + nullAssetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_InvalidAssetId() throws Exception {
        UUID invalidAssetId = UUID.randomUUID();
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + invalidAssetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void purchaseRequestForAvailableAsset_InvalidAccessCode() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("654321", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void purchaseRequestForAvailableAsset_NullAccessCode() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO(null, 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_InsufficientBudget() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 1002);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_InactiveAsset() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_Success() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(100.0)
                .build();

        assetRepository.save(asset);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(PURCHASE_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.assetId").value(asset.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.state").value("REQUESTED"));
    }
}
