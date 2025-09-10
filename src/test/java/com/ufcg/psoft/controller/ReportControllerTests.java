package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ufcg.psoft.commerce.CommerceApplication;
import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.enums.*;
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
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Report controller tests")
class ReportControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    AssetTypeRepository assetTypeRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    WithdrawRepository withdrawRepository;

    private UUID clientId;
    private LocalDateTime ts3DaysAgo;
    private LocalDateTime ts1DayAgo;

    private static final String REPORTS_BASE_URL = "/reports";
    private static final String OPERATIONS_ENDPOINT = "/operations";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_ACCESS_CODE = "123456";
    private static final String INVALID_JSON = "{ invalid json }";

    @BeforeEach
    void setup() {
        objectMapper.findAndRegisterModules();

        AssetType stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("No STOCK asset type. Seed missing."));

        WalletModel wallet = WalletModel.builder()
                .budget(50_000.0)
                .build();

        ClientModel client = createClient(
                null,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );

        client = clientRepository.save(client);
        clientId = client.getId();

        wallet = client.getWallet();

        AssetModel stock = AssetModel.builder()
                .name("Test Stock")
                .description("E2E stock")
                .quotation(100.0)
                .quotaQuantity(1_000.0)
                .isActive(true)
                .assetType(stockType)
                .build();
        stock = assetRepository.save(stock);

        ts3DaysAgo = LocalDateTime.now().minusDays(3).withNano(0);
        ts1DayAgo  = LocalDateTime.now().minusDays(1).withNano(0);

        purchaseRepository.save(PurchaseModel.builder()
                .wallet(wallet)
                .asset(stock)
                .quantity(10.0)
                .acquisitionPrice(100.0)
                .date(ts3DaysAgo.toLocalDate())
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build());

        withdrawRepository.save(WithdrawModel.builder()
                .wallet(wallet)
                .asset(stock)
                .quantity(5.0)
                .sellingPrice(120.0)
                .tax(10.0)
                .withdrawValue(590.0)
                .date(ts1DayAgo.toLocalDate())
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build());
    }


    @AfterEach
    void tearDown() {
        withdrawRepository.deleteAll();
        purchaseRepository.deleteAll();
        assetRepository.deleteAll();
        clientRepository.deleteAll();
        walletRepository.deleteAll();
    }

    private String url() {
        return REPORTS_BASE_URL + OPERATIONS_ENDPOINT;
    }

    private String body(OperationReportRequestDTO dto) throws Exception {
        return objectMapper.writeValueAsString(dto);
    }

    private ClientModel createClient(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode,
                                     AddressModel address, PlanTypeEnum planType, WalletModel wallet) {
        return new ClientModel(id, fullName, email, accessCode, address, planType, wallet);
    }

    @Test
    @DisplayName("Should list both purchases and withdraws without filters")
    void list_noFilters_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operationType", is(OperationTypeEnum.WITHDRAW.name())))
                .andExpect(jsonPath("$[0].assetName", is("Test Stock")))
                .andExpect(jsonPath("$[0].grossValue", is(600.0)))
                .andExpect(jsonPath("$[0].tax", is(10.0)))
                .andExpect(jsonPath("$[0].netValue", is(590.0)))
                .andExpect(jsonPath("$[0].occurredAt", notNullValue()))
                .andExpect(jsonPath("$[1].operationType", is(OperationTypeEnum.PURCHASE.name())))
                .andExpect(jsonPath("$[1].assetName", is("Test Stock")))
                .andExpect(jsonPath("$[1].grossValue", is(1000.0)))
                .andExpect(jsonPath("$[1].tax").doesNotExist())
                .andExpect(jsonPath("$[1].netValue").doesNotExist())
                .andExpect(jsonPath("$[1].occurredAt", notNullValue()));
    }

    @Test
    @DisplayName("Should filter by assetType = STOCK")
    void list_filter_assetType_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .assetType(AssetTypeEnum.STOCK)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should filter by operationType = PURCHASE")
    void list_filter_operationType_purchase_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.PURCHASE)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].operationType", is(OperationTypeEnum.PURCHASE.name())))
                .andExpect(jsonPath("$[0].grossValue", is(1000.0)))
                .andExpect(jsonPath("$[0].tax").doesNotExist())
                .andExpect(jsonPath("$[0].netValue").doesNotExist())
                .andExpect(jsonPath("$[0].occurredAt", notNullValue()));
    }

    @Test
    @DisplayName("Should filter by operationType = WITHDRAW")
    void list_filter_operationType_withdraw_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.WITHDRAW)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].operationType", is(OperationTypeEnum.WITHDRAW.name())))
                .andExpect(jsonPath("$[0].grossValue", is(600.0)))
                .andExpect(jsonPath("$[0].tax", is(10.0)))
                .andExpect(jsonPath("$[0].netValue", is(590.0)))
                .andExpect(jsonPath("$[0].occurredAt", notNullValue()));
    }

    @Test
    @DisplayName("Should filter by clientId")
    void list_filter_clientId_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(clientId)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should filter by date range")
    void list_filter_dateRange_success() throws Exception {
        LocalDate from = ts1DayAgo.toLocalDate().minusDays(1);
        LocalDate to = ts1DayAgo.toLocalDate().plusDays(1);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(from)
                .dateTo(to)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].operationType", is(OperationTypeEnum.WITHDRAW.name())))
                .andExpect(jsonPath("$[0].occurredAt", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 when body is invalid JSON")
    void list_invalidJson_returns400() throws Exception {
        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when adminEmail is missing")
    void list_missingAdminEmail_returns400() throws Exception {
        OperationReportRequestDTO bad = OperationReportRequestDTO.builder()
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when dateFrom is after dateTo")
    void list_invalidDateRange_returns400() throws Exception {
        OperationReportRequestDTO bad = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(LocalDate.now())
                .dateTo(LocalDate.now().minusDays(1))
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return empty list when there is no data")
    void list_empty_success() throws Exception {
        withdrawRepository.deleteAll();
        purchaseRepository.deleteAll();

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE).build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty when filtering by unknown clientId")
    void list_unknownClient_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(UUID.randomUUID())
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should include operation at lower date bound")
    void list_inclusiveLowerDate_success() throws Exception {
        LocalDate bound = ts1DayAgo.toLocalDate();

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(bound)
                .dateTo(bound)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].operationType", is(OperationTypeEnum.WITHDRAW.name())))
                .andExpect(jsonPath("$[0].occurredAt", startsWith(bound.toString())));
    }


    @Test
    @DisplayName("Should include operation at upper date bound")
    void list_inclusiveUpperDate_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(LocalDate.now().minusYears(1))
                .dateTo(ts1DayAgo.toLocalDate())
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].occurredAt", not(empty())));
    }

    @Test
    @DisplayName("Should return empty for assetType=CRYPTO when only STOCK exists")
    void list_assetTypeCrypto_noData_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE)
                .assetType(AssetTypeEnum.CRYPTO)
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should still list historical operations for inactive asset")
    void list_inactiveAsset_stillShown_success() throws Exception {
        assetRepository.findAll().forEach(a -> { a.setActive(false); assetRepository.save(a); });

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE).build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should combine filters: operationType=WITHDRAW and assetType=STOCK")
    void list_composedFilters_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.WITHDRAW)
                .assetType(AssetTypeEnum.STOCK)
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].operationType", everyItem(is(OperationTypeEnum.WITHDRAW.name()))));
    }

    @Test
    @DisplayName("Should pick only purchase when date range targets purchase timestamp")
    void list_dateRange_onlyPurchase_success() throws Exception {
        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL).adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(ts3DaysAgo.toLocalDate().minusDays(1))
                .dateTo(ts3DaysAgo.toLocalDate().plusDays(1))
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].operationType", everyItem(is(OperationTypeEnum.PURCHASE.name()))));
    }

    @Test
    @DisplayName("Should return 401 with wrong admin credentials")
    void list_wrongAdmin_unauthorized() throws Exception {
        OperationReportRequestDTO bad = OperationReportRequestDTO.builder()
                .adminEmail("wrong@example.com")
                .adminAccessCode("wrong")
                .build();

        mockMvc.perform(post(url()).contentType(MediaType.APPLICATION_JSON).content(body(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should honor ordering when occurredAt ties")
    void list_order_stable_when_same_timestamp() throws Exception {
        WalletModel wallet = clientRepository.findById(clientId).orElseThrow().getWallet();
        AssetModel asset = assetRepository.findAll().stream().findFirst().orElseThrow();

        LocalDate sameDate = ts1DayAgo.toLocalDate();

        withdrawRepository.save(WithdrawModel.builder()
                .wallet(wallet)
                .asset(asset)
                .quantity(2.0)
                .sellingPrice(121.0)
                .tax(5.0)
                .withdrawValue(237.0)
                .date(sameDate)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build());

        withdrawRepository.save(WithdrawModel.builder()
                .wallet(wallet)
                .asset(asset)
                .quantity(3.0)
                .sellingPrice(122.0)
                .tax(7.0)
                .withdrawValue(359.0)
                .date(sameDate)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build());

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.WITHDRAW)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].occurredAt", everyItem(notNullValue())));
    }
    @Test
    @DisplayName("Should filter by client across multiple clients")
    void list_filter_client_multiple_success() throws Exception {
        AssetType stockType = assetTypeRepository.findByName("STOCK").orElseThrow();

        WalletModel otherWallet = WalletModel.builder().budget(10_000.0).build();
        ClientModel otherClient = createClient(
                null,
                "Bruno Test",
                new EmailModel("bruno@test.com"),
                new AccessCodeModel("654321"),
                new AddressModel("Rua B", "321", "Bairro", "Cidade", "UF", "BR", "99999-999"),
                PlanTypeEnum.NORMAL,
                otherWallet
        );
        otherClient = clientRepository.save(otherClient);

        AssetModel otherAsset = assetRepository.save(AssetModel.builder()
                .name("Other Stock")
                .description("Second client asset")
                .quotation(50.0)
                .quotaQuantity(500.0)
                .isActive(true)
                .assetType(stockType)
                .build());

        purchaseRepository.save(PurchaseModel.builder()
                .wallet(otherWallet)
                .asset(otherAsset)
                .quantity(4.0)
                .acquisitionPrice(50.0)
                .date(LocalDate.now().minusDays(2))
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build());

        withdrawRepository.save(WithdrawModel.builder()
                .wallet(otherWallet)
                .asset(otherAsset)
                .quantity(1.0)
                .sellingPrice(60.0)
                .tax(1.0)
                .withdrawValue(59.0)
                .date(LocalDate.now().minusDays(1))
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build());

        OperationReportRequestDTO reqOnlyFirst = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(clientId)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(reqOnlyFirst)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].assetName", everyItem(is("Test Stock"))));
    }
    @Test
    @DisplayName("Should handle large number of operations")
    void list_manyOperations_success() throws Exception {
        WalletModel wallet = clientRepository.findById(clientId).orElseThrow().getWallet();
        AssetModel asset = assetRepository.findAll().stream().findFirst().orElseThrow();

        for (int i = 0; i < 30; i++) {
            purchaseRepository.save(PurchaseModel.builder()
                    .wallet(wallet)
                    .asset(asset)
                    .quantity(1.0 + i)
                    .acquisitionPrice(100.0 + i)
                    .date(LocalDate.now().minusDays(10).plusDays(i % 5))
                    .stateEnum(PurchaseStateEnum.REQUESTED)
                    .build());
        }
        for (int i = 0; i < 30; i++) {
            withdrawRepository.save(WithdrawModel.builder()
                    .wallet(wallet)
                    .asset(asset)
                    .quantity(1.0 + (i % 3))
                    .sellingPrice(110.0 + i)
                    .tax(2.0)
                    .withdrawValue((110.0 + i) * (1.0 + (i % 3)) - 2.0)
                    .date(LocalDate.now().minusDays(i % 7))
                    .stateEnum(WithdrawStateEnum.REQUESTED)
                    .build());
        }

        long expected = purchaseRepository.count() + withdrawRepository.count();

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        mockMvc.perform(post(url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) expected)));
    }
}
