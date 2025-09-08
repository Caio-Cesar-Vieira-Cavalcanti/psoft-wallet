package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.dto.asset.AssetDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.HoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletHoldingResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.model.asset.*;
import com.ufcg.psoft.commerce.model.asset.types.TreasuryBounds;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.model.wallet.*;
import com.ufcg.psoft.commerce.repository.asset.*;
import com.ufcg.psoft.commerce.repository.client.*;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.observer.EventManagerImpl;

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
import com.ufcg.psoft.commerce.CommerceApplication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Client Controller Tests")
class ClientControllerTests {

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

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private EventManagerImpl eventManager;

    private static final String CLIENT_BASE_URL = "/clients";
    private static final String ASSETS_ENDPOINT = "/assets";
    private static final String WALLET = "/wallet";
    private static final String WALLET_HOLDING = "/wallet-holding";
    private static final String PURCHASES_ENDPOINT = "/purchase";
    private static final String STOCK_ASSET_TYPE_NAME = "STOCK";
    private static final String INTEREST = "/interest";
    private static final String PRICE_VARIATION = "/price-variation";
    private static final String AVAILABILITY = "/availability";

    private AssetType stockType;
    private UUID clientId;
    private UUID walletId;

    @BeforeEach
    void setup() {
        WalletModel tempWallet = WalletModel.builder()
                .budget(10000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                tempWallet
        );
        clientId = clientRepository.save(client).getId();
        walletId = walletRepository.save(tempWallet).getId();

        stockType = assetTypeRepository.findByName(STOCK_ASSET_TYPE_NAME)
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));
    }

    @AfterEach
    void tearDown() {
        purchaseRepository.deleteAll();
        clientRepository.deleteAll();
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
        return PurchaseModel.builder()
                .id(id)
                .asset(asset)
                .wallet(wallet)
                .quantity(quantity)
                .date(date)
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();
    }

    @Test
    @DisplayName("Shoud create client successfully")
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
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.planType").value("NORMAL"));
    }

    @Test
    @DisplayName("Should get client by ID successfully")
    void testGetClientById_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Azevedo"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDeleteClient_Success() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should patch client full name successfully")
    void testPatchClientFullName_Success() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "123456");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Carlos"));
    }

    @Test
    @DisplayName("Should return all clients successfully")
    void testGetAllClients_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fullName").value("João Azevedo"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testPatchClientFullName_InvalidAccessCode() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "654321");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testDeleteClient_InvalidAccessCode() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("654321");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
    void testDeleteClient_WithInvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        ClientDeleteRequestDTO requestDTO = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
    void testGetClientById_InvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get(CLIENT_BASE_URL + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
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
    @DisplayName("Should return 400 Bad Request when id is null")
    void testGetActiveAssets_WhenClientIdIsNull() throws Exception {
        UUID randomClientId = null;

        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("123456");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + randomClientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testGetActiveAssets_WhenAccessCodeIsInvalid() throws Exception {
        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("654321");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is null")
    void testGetActiveAssets_WhenAccessCodeIsNull() throws Exception {
        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode(null);

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is missing")
    void testGetActiveAssets_WhenAccessCodeIsMissing() throws Exception {
        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return the active assets available")
    void testGetActiveAssetsForPremiumClient_Successful() throws Exception {
        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("123456");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return the active assets available")
    void testGetActiveAssetsForNormalClient_Successful() throws Exception {
        WalletModel wallet = WalletModel.builder()
                .budget(10000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                clientId,
                "Rafael Barreto",
                new EmailModel("rafael@email.com"),
                new AccessCodeModel("654321"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.NORMAL,
                wallet
        );
        UUID newClientId = clientRepository.save(client).getId();

        ClientActiveAssetsRequestDTO requestDTO = new ClientActiveAssetsRequestDTO();
        requestDTO.setAccessCode("654321");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + newClientId + ASSETS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return asset details for client successfully")
    void testGetAssetDetailsForClient_Success() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(5000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Lucas Pereira",
                new EmailModel("lucas@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "456", "Bairro", "Cidade", "Estado", "Brasil", "11111-111"),
                PlanTypeEnum.NORMAL,
                wallet
        );

        client = clientRepository.save(client);

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + client.getId() + ASSETS_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(asset.getId().toString()))
                .andExpect(jsonPath("$.name").value(asset.getName()))
                .andExpect(jsonPath("$.description").value(asset.getDescription()))
                .andExpect(jsonPath("$.quotation").value(asset.getQuotation()))
                .andExpect(jsonPath("$.quotaQuantity").value(asset.getQuotaQuantity()));
    }

    @Test
    @DisplayName("Should return 401 when access code is invalid")
    void testGetAssetDetailsForClient_InvalidAccessCode() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(5000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Lucas Pereira",
                new EmailModel("lucas@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "456", "Bairro", "Cidade", "Estado", "Brasil", "11111-111"),
                PlanTypeEnum.NORMAL,
                wallet
        );

        client = clientRepository.save(client);

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("wrong-code")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + client.getId() + ASSETS_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 when clientId is invalid")
    void testGetAssetDetailsForClient_ClientNotFound() throws Exception {
        UUID invalidClientId = UUID.randomUUID();
        AssetModel asset = createAndSaveAsset(stockType);

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + invalidClientId + ASSETS_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when assetId is invalid")
    void testGetAssetDetailsForClient_AssetNotFound() throws Exception {
        UUID invalidAssetId = UUID.randomUUID();

        WalletModel wallet = WalletModel.builder()
                .budget(5000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Lucas Pereira",
                new EmailModel("lucas@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "456", "Bairro", "Cidade", "Estado", "Brasil", "11111-111"),
                PlanTypeEnum.NORMAL,
                wallet
        );

        client = clientRepository.save(client);

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + client.getId() + ASSETS_ENDPOINT + "/" + invalidAssetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when assetId is invalid")
    void testMarkInterestInAsset_WithInvalidAssetId() throws Exception {
        UUID invalidAssetId = UUID.randomUUID();

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(invalidAssetId)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when assetId is null")
    void testMarkInterestInAsset_WithNullAssetId() throws Exception {
        UUID nullAssetId = null;

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(nullAssetId)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when assetId is missing")
    void testMarkInterestInAsset_WithMissingAssetId() throws Exception {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when clientId is invalid")
    void testMarkInterestInAsset_WithInvalidClientId() throws Exception {
        UUID invalidClientId = UUID.randomUUID();
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + invalidClientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + invalidClientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when clientId is null")
    void testMarkInterestInAsset_WithNullClientId() throws Exception {
        UUID nullClientId = null;
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + nullClientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + nullClientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when access code is invalid")
    void testMarkInterestInAsset_WithInvalidAccessCode() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("654321")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when access code is null")
    void testMarkInterestInAsset_WithNullAccessCode() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode(null)
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when access code is missing")
    void testMarkInterestInAsset_WithMissingAccessCode() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 because the asset isn't stock or crypto")
    void testMarkInterestInPriceVariation_NeitherStockOrCrypto() throws Exception {
        AssetType mockAssetType = new TreasuryBounds();
        mockAssetType.setName("TREASURY_BOUNDS");
        mockAssetType.setId(1L);

        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(mockAssetType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetRepository.save(asset);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 because the asset is inactive")
    void testMarkInterestInPriceVariation_InactiveAsset() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 because the client doesn't have a premium plan")
    void testMarkInterestInPriceVariation_NormalClient() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetRepository.save(asset);

        WalletModel wallet = WalletModel.builder()
                .budget(5000)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Lucas Pereira",
                new EmailModel("lucas@email.com"),
                new AccessCodeModel("654321"),
                new AddressModel("Street", "456", "Bairro", "Cidade", "Estado", "Brasil", "11111-111"),
                PlanTypeEnum.NORMAL,
                wallet
        );

        UUID otherClientId = clientRepository.save(client).getId();

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("654321")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + otherClientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should mark interest successfully in the price variation from an asset")
    void testMarkInterestInPriceVariation_Successful() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test 2")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetRepository.save(asset);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + PRICE_VARIATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 because you can't mark availability interest in a active asset")
    void testMarkInterestInAvailability_ActiveAsset() throws Exception {
        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test")
                .isActive(true)
                .assetType(stockType)
                .description("Default asset for this test")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetRepository.save(asset);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should mark interest successfully when asset gets active")
    void testMarkInterestInAvailability_Successful() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .accessCode("123456")
                .assetId(asset.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENT_BASE_URL + "/" + clientId + INTEREST + AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
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
    @DisplayName("Should return 409 Conflict when trying to delete asset referenced in purchases")
    void testDeleteAsset_WhenReferencedInPurchases_ReturnsConflict() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        WalletModel wallet = WalletModel.builder()
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = createClient(
                UUID.randomUUID(),
                "Test User",
                new EmailModel("testuser@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );
        clientRepository.save(client);
        walletRepository.save(wallet);

        AssetDeleteRequestDTO deleteRequestDTO = AssetDeleteRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        PurchaseModel purchase1 = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1), wallet);
        PurchaseModel purchase2 = createPurchase(UUID.randomUUID(), asset, 3.0, LocalDate.now().minusDays(2), wallet);

        purchaseRepository.saveAll(List.of(purchase1, purchase2));

        mockMvc.perform(delete(ASSETS_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete asset: it is referenced in purchases"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testGetPurchaseHistory_WhenAccessCodeIsInvalid() throws Exception {
        ClientPurchaseHistoryRequestDTO requestDTO = new ClientPurchaseHistoryRequestDTO();
        requestDTO.setAccessCode("invalid_code");

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
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

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
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

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
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

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
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

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + client.getId() + WALLET + PURCHASES_ENDPOINT)
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

        mockMvc.perform(post("/clients/{clientId}/wallet/purchase/{purchaseId}/confirmation-by-client",
                        client.getId(), purchase.getId())
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

        PurchaseModel purchase1 = createPurchase(UUID.randomUUID(), asset, 5.0, LocalDate.now().minusDays(1), wallet);
        purchase1 = purchaseRepository.save(purchase1);

        PurchaseModel purchase2 = createPurchase(UUID.randomUUID(), asset, 3.0, LocalDate.now().minusDays(1), wallet);
        purchase2 = purchaseRepository.save(purchase2);

        PurchaseConfirmationByClientDTO requestDto = PurchaseConfirmationByClientDTO.builder()
                .accessCode(client.getAccessCode().getAccessCode())
                .build();

        mockMvc.perform(post("/clients/{clientId}/wallet/purchase/{purchaseId}/confirmation-by-client",
                        client.getId(), purchase1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/clients/{clientId}/wallet/purchase/{purchaseId}/confirmation-by-client",
                        client.getId(), purchase2.getId())
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
    @Transactional
    @DisplayName("Should get client wallet holdings successfully")
    void testGetClientWalletHolding_Success_Alternative() throws Exception {
        WalletModel clientWallet = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId))
                .getWallet();

        AssetModel asset1 = createAndSaveAsset(stockType);
        AssetModel asset2 = createAndSaveAsset(stockType);

        HoldingModel holding1 = HoldingModel.builder()
                .asset(asset1)
                .quantity(10.0)
                .wallet(clientWallet)
                .build();

        HoldingModel holding2 = HoldingModel.builder()
                .asset(asset2)
                .quantity(25.0)
                .wallet(clientWallet)
                .build();

        clientWallet.getHoldings().put(asset1.getId(), holding1);
        clientWallet.getHoldings().put(asset2.getId(), holding2);
        walletRepository.save(clientWallet);

        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("123456")
                .build();


        String responseContent = mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET_HOLDING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet.walletId").value(clientWallet.getId().toString()))
                .andExpect(jsonPath("$.wallet.budget").value(clientWallet.getBudget()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        WalletHoldingResponseDTO responseDto = objectMapper.readValue(responseContent, WalletHoldingResponseDTO.class);

        assertEquals(2, responseDto.getHoldings().size());

        List<UUID> assetIdsInResponse = responseDto.getHoldings().stream()
                .map(HoldingResponseDTO::getAssetId)
                .toList();

        assertTrue(assetIdsInResponse.contains(asset1.getId()));
        assertTrue(assetIdsInResponse.contains(asset2.getId()));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when access code is invalid")
    void testGetClientWalletHolding_InvalidAccessCode() throws Exception {
        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("654321")
                .build();


        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET_HOLDING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 Not Found when client ID is invalid")
    void testGetClientWalletHolding_InvalidClientId() throws Exception {
        UUID invalidId = UUID.randomUUID();
        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + invalidId + WALLET_HOLDING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is null")
    void testGetClientWalletHolding_NullAccessCode() throws Exception {
        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode(null)
                .build();

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET_HOLDING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when access code is missing")
    void testGetClientWalletHolding_MissingAccessCode() throws Exception {
        String jsonWithoutAccessCode = "{}";

        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId + WALLET_HOLDING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutAccessCode))
                .andExpect(status().isBadRequest());
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + nullClientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + invalidClientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + nullAssetId)
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + invalidAssetId)
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseRequestForAvailableAsset_InactiveAsset() throws Exception {
        AssetModel asset = createAndSaveAsset(stockType);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 5);

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
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

        mockMvc.perform(post(CLIENT_BASE_URL + "/" + clientId + WALLET + PURCHASES_ENDPOINT + "/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.assetId").value(asset.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.state").value("REQUESTED"));
    }
}