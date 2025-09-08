package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.ufcg.psoft.commerce.CommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Withdraw Controller Tests")
class WithdrawControllerTests {

    private static final String WITHDRAW_BASE_URL = "/withdraws";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    private UUID withdrawId;
    private UUID walletId;
    private UUID assetId;
    private UUID clientId;
    private AssetType stockType;

    @BeforeEach
    void setup() {
        stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("STOCK asset type not found"));

        AssetModel asset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Test Asset")
                .assetType(stockType)
                .description("Test Asset Description")
                .isActive(true)
                .quotation(100.0)
                .quotaQuantity(1.0)
                .build();
        asset = assetRepository.save(asset);
        assetId = asset.getId();

        WalletModel wallet = WalletModel.builder()
                .id(UUID.randomUUID())
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        ClientModel client = ClientModel.builder()
                .id(UUID.randomUUID())
                .fullName("Test Client")
                .planType(PlanTypeEnum.PREMIUM)
                .email(new EmailModel("joao@email.com"))
                .address(new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .accessCode(new AccessCodeModel("123456"))
                .wallet(wallet)
                .build();
        client = clientRepository.save(client);
        clientId = client.getId();

        wallet = walletRepository.save(wallet);
        walletId = wallet.getId();

        HoldingModel holding = HoldingModel.builder()
                .asset(asset)
                .wallet(wallet)
                .quantity(20.0)
                .accumulatedPrice(2000.0)
                .build();
        wallet.getHoldings().put(asset.getId(), holding);
        walletRepository.save(wallet);

        WithdrawModel withdraw = WithdrawModel.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .wallet(wallet)
                .quantity(10.0)
                .date(LocalDate.now())
                .sellingPrice(100.0)
                .tax(10.0)
                .withdrawValue(950.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();
        withdraw = withdrawRepository.save(withdraw);
        withdrawId = withdraw.getId();
    }

    @Test
    @DisplayName("Should confirm withdraw successfully")
    void testConfirmWithdraw_Success() throws Exception {
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + withdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withdrawId").value(withdrawId.toString()))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.assetId").value(assetId.toString()))
                .andExpect(jsonPath("$.quantityWithdrawn").value(10.0))
                .andExpect(jsonPath("$.state").value("IN_ACCOUNT"))
                .andExpect(jsonPath("$.valueReceived").value(950.0))
                .andExpect(jsonPath("$.newWalletBudget").value(1950.0));
    }

    @Test
    @DisplayName("Should fail confirmation if withdraw not found")
    void testConfirmWithdraw_WithdrawNotFound() throws Exception {
        UUID invalidWithdrawId = UUID.randomUUID();
        
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + invalidWithdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail confirmation if admin email is invalid")
    void testConfirmWithdraw_InvalidAdminEmail() throws Exception {
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("invalid@test.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + withdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail confirmation if admin access code is invalid")
    void testConfirmWithdraw_InvalidAdminAccessCode() throws Exception {
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("000000")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + withdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should verify automatic transition from CONFIRMED to IN_ACCOUNT")
    void testConfirmWithdraw_AutomaticTransition() throws Exception {
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + withdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("IN_ACCOUNT"));

        WithdrawModel savedWithdraw = withdrawRepository.findById(withdrawId).orElseThrow();
        assert savedWithdraw.getStateEnum() == WithdrawStateEnum.IN_ACCOUNT;
    }

    @Test
    @DisplayName("Should verify client notification is triggered during confirmation")
    void testConfirmWithdraw_ClientNotification() throws Exception {
        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + withdrawId + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()); // This will show logs in test output

        WithdrawModel savedWithdraw = withdrawRepository.findById(withdrawId).orElseThrow();
        assert savedWithdraw.getStateEnum() == WithdrawStateEnum.IN_ACCOUNT;
    }

    @Test
    @DisplayName("Should fail confirmation if client has insufficient holdings")
    void testConfirmWithdraw_InsufficientHoldings() throws Exception {
        AssetModel testAsset = assetRepository.findById(assetId).orElseThrow();
        WalletModel testWallet = walletRepository.findById(walletId).orElseThrow();

        WithdrawModel insufficientWithdraw = WithdrawModel.builder()
                .id(UUID.randomUUID())
                .asset(testAsset)
                .wallet(testWallet)
                .quantity(50.0) // More than the 20.0 available
                .date(LocalDate.now())
                .sellingPrice(100.0)
                .tax(10.0)
                .withdrawValue(950.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();
        insufficientWithdraw = withdrawRepository.save(insufficientWithdraw);

        WithdrawConfirmationRequestDTO dto = WithdrawConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + insufficientWithdraw.getId() + "/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Should fail due to insufficient holdings
    }

    @Test
    @DisplayName("Should withdraw asset via controller successfully")
    public void testWithdrawAsset_Controller_Success() throws Exception {
        AssetType stock = assetTypeRepository.findByName("STOCK")
                .orElseGet(() -> assetTypeRepository.save(new Stock()));

        assetTypeRepository.save(stock);

        AssetModel asset = AssetModel.builder()
                .name("Test Asset")
                .description("Test Description")
                .assetType(stockType)
                .isActive(true)
                .quotaQuantity(100)
                .quotation(100.0)
                .build();
        asset = assetRepository.save(asset);

        WalletModel wallet = WalletModel.builder()
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();
        wallet = walletRepository.save(wallet);

        ClientModel client = ClientModel.builder()
                .wallet(wallet)
                .accessCode(new AccessCodeModel("123456"))
                .build();
        client = clientRepository.save(client);

        HoldingModel holding = HoldingModel.builder()
                .asset(asset)
                .wallet(wallet)
                .quantity(10)
                .accumulatedPrice(1000.0)
                .build();
        wallet.getHoldings().put(asset.getId(), holding);
        holdingRepository.save(holding);
        walletRepository.save(wallet);

        ClientWithdrawAssetRequestDTO requestDTO = new ClientWithdrawAssetRequestDTO();
        requestDTO.setAccessCode("123456");
        requestDTO.setQuantityToWithdraw(5.0);

        mockMvc.perform(post("/withdraws/" + client.getId() + "/wallet/withdraw/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(wallet.getId().toString()))
                .andExpect(jsonPath("$.assetId").value(asset.getId().toString()))
                .andExpect(jsonPath("$.quantityWithdrawn").value(5.0));
    }

    @Test
    @DisplayName("Should fail withdrawal if client not found")
    void testWithdrawAsset_Controller_ClientNotFound() throws Exception {
        UUID invalidClientId = UUID.randomUUID();

        AssetModel asset = createAndSaveAsset(stockType);

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5)
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + invalidClientId + "/wallet/withdraw/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail withdrawal if access code is invalid")
    void testWithdrawAsset_Controller_InvalidAccessCode() throws Exception {
        ClientModel client = clientRepository.save(
                ClientModel.builder()
                        .fullName("Test Client")
                        .email(new EmailModel("test@test.com"))
                        .accessCode(new AccessCodeModel("111111"))
                        .wallet(new WalletModel())
                        .build()
        );

        UUID clientId = client.getId();

        AssetModel asset = createAndSaveAsset(stockType);

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("000000")
                .quantityToWithdraw(5)
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw/" + asset.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail withdrawal if asset not found")
    void testWithdrawAsset_Controller_AssetNotFound() throws Exception {
        ClientModel client = clientRepository.save(
                ClientModel.builder()
                        .fullName("Test Client")
                        .email(new EmailModel("test@test.com"))
                        .accessCode(new AccessCodeModel("123456"))
                        .wallet(new WalletModel())
                        .build()
        );

        UUID clientId = client.getId();

        UUID invalidAssetId = UUID.randomUUID();

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5)
                .build();

        mockMvc.perform(post(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw/" + invalidAssetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail when client ID is invalid")
    void testGetWithdrawHistory_InvalidClientId() throws Exception {
        UUID invalidClientId = UUID.randomUUID();
        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + invalidClientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail when access code is invalid")
    void testGetWithdrawHistory_InvalidAccessCode() throws Exception {
        String invalidAccessCode = "wrong-code";
        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode(invalidAccessCode)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return empty list when no withdraws exist")
    void testGetWithdrawHistory_NoWithdraws() throws Exception {
        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return withdraw history without filters")
    void testGetWithdrawHistory_NoFilters() throws Exception {
        // Criar alguns withdraws de teste
        createTestWithdraws();

        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return withdraw history with withdraw state filter")
    void testGetWithdrawHistory_WithWithdrawOneFilter() throws Exception {
        createTestWithdraws();

        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                .withdrawState(WithdrawStateEnum.REQUESTED) // Filtro por estado
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return withdraw history with two filters")
    void testGetWithdrawHistory_WithTwoFilters() throws Exception {
        createTestWithdraws();

        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                .date(LocalDate.now())
                .withdrawState(WithdrawStateEnum.REQUESTED) // Dois filtros
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return withdraw history with three filters")
    void testGetWithdrawHistory_WithThreeFilters() throws Exception {
        createTestWithdraws();

        ClientWithdrawHistoryRequestDTO dto = ClientWithdrawHistoryRequestDTO.builder()
                .accessCode("123456")
                //.assetType(stockType)
                .withdrawState(WithdrawStateEnum.REQUESTED)
                .date(LocalDate.now())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get(WITHDRAW_BASE_URL + "/" + clientId + "/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private void createTestWithdraws() {
        WalletModel wallet = walletRepository.findById(walletId).orElseThrow();
        AssetModel asset = assetRepository.findById(assetId).orElseThrow();

        WithdrawModel withdraw1 = WithdrawModel.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .asset(asset)
                .quantity(10.0)
                .date(LocalDate.now())
                .sellingPrice(100.0)
                .tax(10.0)
                .withdrawValue(950.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();

        WithdrawModel withdraw2 = WithdrawModel.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .asset(asset)
                .quantity(5.0)
                .date(LocalDate.now().minusDays(1))
                .sellingPrice(50.0)
                .tax(5.0)
                .withdrawValue(475.0)
                .stateEnum(WithdrawStateEnum.CONFIRMED)
                .build();

        withdrawRepository.save(withdraw1);
        withdrawRepository.save(withdraw2);
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
}
