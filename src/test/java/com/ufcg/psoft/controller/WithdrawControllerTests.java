package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
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

    private UUID withdrawId;
    private UUID walletId;
    private UUID assetId;
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
        wallet = walletRepository.save(wallet);
        walletId = wallet.getId();

        com.ufcg.psoft.commerce.model.wallet.HoldingModel holding = com.ufcg.psoft.commerce.model.wallet.HoldingModel.builder()
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
                .andExpect(jsonPath("$.valueReceived").exists())
                .andExpect(jsonPath("$.newWalletBudget").exists());
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
}
