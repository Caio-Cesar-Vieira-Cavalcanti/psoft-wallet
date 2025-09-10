package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.wallet.ExportTransactionsRequestDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Transaction controller tests")
public class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private WithdrawRepository withdrawRepository;

    private AssetType stockType;
    private UUID clientId;
    private AssetModel asset;
    private ClientModel client;

    private static final String TRANSACTION_URL = "/transactions";
    private static final String TRANSACTION_EXPORT_URL = TRANSACTION_URL + "/export";

    @BeforeEach
    void setup() {
        stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));

        asset = AssetModel.builder()
                .name("Tesla Stock")
                .assetType(stockType)
                .description("Ações da Tesla")
                .quotation(850.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .build();

        assetRepository.save(asset);

        WalletModel wallet = WalletModel.builder()
                .budget(10000)
                .holdings(new HashMap<>())
                .build();

        client = ClientModel.builder()
                .fullName("João Azevedo")
                .email(new EmailModel("joao@email.com"))
                .accessCode(new AccessCodeModel("123456"))
                .address(new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .planType(PlanTypeEnum.PREMIUM)
                .wallet(wallet)
                .build();

        clientId = clientRepository.save(client).getId();
    }

    @AfterEach
    void teardown() {
        purchaseRepository.deleteAll();
        withdrawRepository.deleteAll();
        assetRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    @DisplayName("Export CSV should fail (401) because accessCode is invalid")
    void exportCSVShouldFail401AccessCodeInvalid() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("654321");

        mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Unauthorized client access: access code is incorrect"));
    }

    @Test
    @DisplayName("Export CSV should fail (404) because client not exists")
    void exportCSVShouldFail404ClientNotExists() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("123456");

        mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", startsWith("Client not found with ID")));
    }

    @Test
    @DisplayName("Export CSV should get 200 code without (0) transactions")
    void exportCSVShouldSuccess200ExportWithOutTransactions() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("123456");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().is2xxSuccessful())
            .andReturn();

        byte[] contentBytes = result.getResponse().getContentAsByteArray();
        String csvContent = new String(contentBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("type,asset,quantity,total-value,tax,date,state"));
        assertEquals(1, csvContent.lines().count());
    }

    @Test
    @DisplayName("Export CSV should get 200 code with 1 purchase")
    void exportCSVShouldSuccess200ExportWith1Purchase() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("123456");

        PurchaseModel purchase1 = PurchaseModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(2)
                .date(LocalDate.now())
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();

        purchaseRepository.save(purchase1);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        byte[] contentBytes = result.getResponse().getContentAsByteArray();
        String csvContent = new String(contentBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("type,asset,quantity,total-value,tax,date,state"));
        assertEquals(2, csvContent.lines().count());
    }

    @Test
    @DisplayName("Export CSV should get 200 code with 1 purchase and 1 withdraw")
    void exportCSVShouldSuccess200ExportWith1Withdraw() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("123456");

        PurchaseModel purchase1 = PurchaseModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(2)
                .date(LocalDate.now())
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();

        purchaseRepository.save(purchase1);

        WithdrawModel withdraw1 = WithdrawModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(1)
                .date(LocalDate.now())
                .sellingPrice(950.00)
                .tax(50.00)
                .withdrawValue(900.00)
                .stateEnum(WithdrawStateEnum.IN_ACCOUNT)
                .build();

        withdrawRepository.save(withdraw1);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        byte[] contentBytes = result.getResponse().getContentAsByteArray();
        String csvContent = new String(contentBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("type,asset,quantity,total-value,tax,date,state"));
        assertEquals(3, csvContent.lines().count());
    }

    @Test
    @DisplayName("Export CSV should get 200 code with 2 purchases and 1 withdraw")
    void exportCSVShouldSuccess200ExportWith2PurchasesAnd1Withdraw() throws Exception {
        ExportTransactionsRequestDTO dto = new ExportTransactionsRequestDTO();
        dto.setAccessCode("123456");

        PurchaseModel purchase1 = PurchaseModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(2)
                .date(LocalDate.now())
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();

        purchaseRepository.save(purchase1);

        PurchaseModel purchase2 = PurchaseModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(3)
                .date(LocalDate.now())
                .acquisitionPrice(asset.getQuotation())
                .stateEnum(PurchaseStateEnum.IN_WALLET)
                .build();

        purchaseRepository.save(purchase2);

        WithdrawModel withdraw1 = WithdrawModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(1)
                .date(LocalDate.now())
                .sellingPrice(950.00)
                .tax(50.00)
                .withdrawValue(900.00)
                .stateEnum(WithdrawStateEnum.IN_ACCOUNT)
                .build();

        withdrawRepository.save(withdraw1);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(TRANSACTION_EXPORT_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        byte[] contentBytes = result.getResponse().getContentAsByteArray();
        String csvContent = new String(contentBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("type,asset,quantity,total-value,tax,date,state"));
        assertEquals(4, csvContent.lines().count());
    }



}
