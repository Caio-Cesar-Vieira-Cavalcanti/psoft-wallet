package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.dto.asset.AssetDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.PurchaseState;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.Transaction;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.ufcg.psoft.commerce.CommerceApplication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Client Controller Tests")
public class ClientControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    private static final String CLIENT_BASE_URL = "/clients";
    private static final String ASSETS_ENDPOINT = "/assets";
    private static final String PURCHASES_ENDPOINT = "/purchases";
    private static final String STOCK_ASSET_TYPE_NAME = "STOCK";

    private AssetType stockType;
    private UUID clientId;

    @BeforeEach
    void setup() {
        ClientModel client = createClient(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                new WalletModel()
        );
        clientId = clientRepository.save(client).getId();

        stockType = assetTypeRepository.findByName(STOCK_ASSET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
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

    @Test
    @DisplayName("Create client successfully")
    void testCreateClient_Success() throws Exception {
        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .fullName("Maria Silva")
                .email("maria@email.com")
                .accessCode("654321")
                .budget(15000.0)
                .planType(PlanTypeEnum.NORMAL)
                .address(new AddressDTO("Street", "321", "Neighboorhood", "City", "State", "Country", "98765-432"))
                .build();

        mockMvc.perform(post(CLIENT_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Maria Silva"))
                .andExpect(jsonPath("$.budget").value(15000.0));
    }

    @Test
    @DisplayName("Get client by ID successfully")
    void testGetClientById_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Azevedo"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("Delete client successfully")
    void testDeleteClient_Success() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patch client full name successfully")
    void testPatchClientFullName_Success() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "123456");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Carlos"));
    }

    @Test
    @DisplayName("Get all clients")
    void testGetAllClients_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fullName").value("João Azevedo"));
    }

    @Test
    @DisplayName("Invalid access code")
    void testPatchClientFullName_InvalidAcessCode() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "654321");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid access code")
    void testDeleteClient_InvalidAccessCode() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("654321");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid id")
    void testDeleteClient_WithInvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        ClientDeleteRequestDTO requestDTO = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Invalid id")
    void testGetClientById_InvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get(CLIENT_BASE_URL + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetActiveAssets_WhenClientIdIsInvalid() throws Exception {
        UUID randomClientId = UUID.randomUUID();

        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("123456");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + randomClientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetActiveAssets_WhenAccessCodeIsInvalid() throws Exception {
        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("654321");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
    void testGetPurchaseHistory_WhenClientIdIsInvalid() throws Exception {
        UUID randomClientId = UUID.randomUUID();

        ClientPurchaseHistoryRequestDTO requestDTO = new ClientPurchaseHistoryRequestDTO();
        requestDTO.setAccessCode("789032");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + randomClientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testGetPurchaseHistory_WhenAccessCodeIsInvalid() throws Exception {
        ClientPurchaseHistoryRequestDTO requestDTO = new ClientPurchaseHistoryRequestDTO();
        requestDTO.setAccessCode("invalid_code");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + this.clientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is missing")
    void testGetPurchaseHistory_Fail_MissingAccessCode() throws Exception {
        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                // não setar accessCode
                .build();

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + this.clientId + PURCHASES_ENDPOINT)
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

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + this.clientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return wallet with empty purchase list")
    void testGetPurchaseHistorySuccessfully_NoPurchases() throws Exception {
        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + this.clientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").isNotEmpty())
                .andExpect(jsonPath("$.purchases").isArray())
                .andExpect(jsonPath("$.purchases", hasSize(0)));
    }

    @Test
    @DisplayName("Should return wallet with purchases")
    void testGetPurchaseHistorySuccessfully_WithPurchases() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        // Crie o Set vazio
        Set<Transaction> purchases = new HashSet<>();

        // Crie wallet com o Set inicializado
        WalletModel wallet = WalletModel.builder().purchases(purchases).build();

        UUID purchaseId1 = UUID.randomUUID();
        UUID purchaseId2 = UUID.randomUUID();

        PurchaseModel purchase1 = createPurchase(purchaseId1, asset, 5.0, LocalDate.now().minusDays(1), wallet);
        PurchaseModel purchase2 = createPurchase(purchaseId2, asset, 3.0, LocalDate.now().minusDays(2), wallet);

        // Adicione as compras no Set
        purchases.add(purchase1);
        purchases.add(purchase2);

        ClientModel clientWithPurchases = createClient(
                UUID.randomUUID(),
                "Walber Araújo",
                new EmailModel("walber@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                wallet
        );

        ClientModel savedClient = clientRepository.save(clientWithPurchases);
        clientId = savedClient.getId();

        ClientPurchaseHistoryRequestDTO dto = ClientPurchaseHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(savedClient.getWallet().getId().toString()))
                .andExpect(jsonPath("$.purchases", hasSize(2)))
                .andExpect(jsonPath("$.purchases[0].quantity").value(5.0))
                .andExpect(jsonPath("$.purchases[1].quantity").value(3.0));
    }

    @Test
    @DisplayName("Should return 409 Conflict when trying to delete asset referenced in purchases")
    void testDeleteAsset_WhenReferencedInPurchases_ReturnsConflict() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder().purchases(new HashSet<>()).build();

        PurchaseModel purchase1 = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1), wallet);
        PurchaseModel purchase2 = createPurchase(UUID.randomUUID(), asset, 3.0, LocalDate.now().minusDays(2), wallet);

        Set<Transaction> purchases = new HashSet<>();
        purchases.add(purchase1);
        purchases.add(purchase2);
        wallet.setPurchases(purchases);

        ClientModel clientWithPurchases = createClient(
                UUID.randomUUID(),
                "Test User",
                new EmailModel("testuser@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                wallet
        );
        clientRepository.save(clientWithPurchases);

        AssetDeleteRequestDTO deleteRequestDTO = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(delete(ASSETS_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete asset: it is referenced in purchases"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    private PurchaseModel createPurchase(UUID id, AssetModel asset, double quantity, LocalDate date, WalletModel wallet) {
        return PurchaseModel.builder()
                .id(id)
                .asset(asset)
                .quantity(quantity)
                .state(PurchaseState.IN_WALLET)
                .date(date)
                .wallet(wallet)
                .build();
    }

    private WalletModel createWalletWithPurchases(Set<Transaction> purchases) {
        return WalletModel.builder()
                .purchases(purchases)
                .build();
    }

    private ClientModel createClient(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode,
                                     AddressModel address, PlanTypeEnum planType, double budget, WalletModel wallet) {
        return new ClientModel(id, fullName, email, accessCode, address, planType, budget, wallet);
    }

    // Other tests related to getActiveAssets are in AssetServiceUnitTests.
}