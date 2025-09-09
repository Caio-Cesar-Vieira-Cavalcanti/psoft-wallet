package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.report.ReportServiceImpl;
import com.ufcg.psoft.commerce.service.report.fetchers.PurchaseFetcher;
import com.ufcg.psoft.commerce.service.report.fetchers.WithdrawFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Report Service Unit Tests")
class ReportServiceUnitTests {

    private ReportServiceImpl reportService;

    private AdminService adminService;
    private DTOMapperService dtoMapperService;
    private PurchaseRepository purchaseRepository;
    private WithdrawRepository withdrawRepository;
    private ClientRepository clientRepository;

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_ACCESS_CODE = "123456";

    private UUID walletId;
    private UUID clientId;
    private WalletModel wallet;
    private ClientModel client;
    private AssetModel stockAsset;

    @BeforeEach
    void setup() {
        adminService = mock(AdminService.class);
        dtoMapperService = mock(DTOMapperService.class);
        purchaseRepository = mock(PurchaseRepository.class);
        withdrawRepository = mock(WithdrawRepository.class);
        clientRepository = mock(ClientRepository.class);

        reportService = new ReportServiceImpl();
        ReflectionTestUtils.setField(reportService, "adminService", adminService);
        ReflectionTestUtils.setField(reportService, "clientRepository", clientRepository);

        PurchaseFetcher purchaseFetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(purchaseFetcher, "purchaseRepository", purchaseRepository);
        ReflectionTestUtils.setField(purchaseFetcher, "dtoMapperService", dtoMapperService);
        ReflectionTestUtils.setField(purchaseFetcher, "clientRepository", clientRepository);

        WithdrawFetcher withdrawFetcher = new WithdrawFetcher();
        ReflectionTestUtils.setField(withdrawFetcher, "withdrawRepository", withdrawRepository);
        ReflectionTestUtils.setField(withdrawFetcher, "dtoMapperService", dtoMapperService);
        ReflectionTestUtils.setField(withdrawFetcher, "clientRepository", clientRepository);

        ReflectionTestUtils.setField(reportService, "fetchers", List.of(purchaseFetcher, withdrawFetcher));

        doNothing().when(adminService).validateAdmin(ADMIN_EMAIL, ADMIN_ACCESS_CODE);

        walletId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        wallet = WalletModel.builder().id(walletId).budget(10_000.0).build();

        client = new ClientModel(
                clientId,
                "Alice",
                new EmailModel("alice@test.com"),
                null, null, null,
                wallet
        );

        stockAsset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("VALE3")
                .description("Stock asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .assetType(new Stock())
                .build();

        when(clientRepository.findAll()).thenReturn(List.of(client));

        when(clientRepository.findById(eq(clientId))).thenReturn(Optional.of(client));

        when(clientRepository.findById(argThat(id -> id != null && !id.equals(clientId))))
                .thenReturn(Optional.empty());
    }


    private PurchaseModel purchase(LocalDate date, double qty, double price) {
        return PurchaseModel.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .asset(stockAsset)
                .quantity(qty)
                .acquisitionPrice(price)
                .date(date)
                .build();
    }

    private WithdrawModel withdraw(LocalDate date, double qty, double sellPrice, Double tax, Double net) {
        return WithdrawModel.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .asset(stockAsset)
                .quantity(qty)
                .sellingPrice(sellPrice)
                .tax(tax)
                .withdrawValue(net)
                .date(date)
                .build();
    }

    private OperationReportResponseDTO stubPurchaseDTO(PurchaseModel p) {
        double quantity = p.getQuantity();
        double gross = p.getQuantity() * p.getAcquisitionPrice();
        LocalDateTime occurredAt = p.getDate().atStartOfDay();

        OperationReportResponseDTO dto = OperationReportResponseDTO.builder()
                .operationId(p.getId())
                .operationType(OperationTypeEnum.PURCHASE)
                .clientId(client.getId())
                .clientName(client.getFullName())
                .assetId(stockAsset.getId())
                .assetName(stockAsset.getName())
                .assetType(AssetTypeEnum.STOCK)
                .quantity(quantity)
                .gross(gross)
                .tax(null)
                .net(null)
                .occurredAt(occurredAt)
                .build();

        when(dtoMapperService.toOperationPurchaseReportDTO(
                eq(p), eq(client),
                eq(quantity), eq(gross),
                isNull(), isNull(),
                eq(occurredAt)
        )).thenReturn(dto);

        return dto;
    }

    private OperationReportResponseDTO stubWithdrawDTO(WithdrawModel w) {
        double quantity = w.getQuantity();
        double gross = w.getQuantity() * w.getSellingPrice();
        Double tax = w.getTax();
        Double net = w.getWithdrawValue();
        LocalDateTime occurredAt = w.getDate().atStartOfDay();

        OperationReportResponseDTO dto = OperationReportResponseDTO.builder()
                .operationId(w.getId())
                .operationType(OperationTypeEnum.WITHDRAW)
                .clientId(client.getId())
                .clientName(client.getFullName())
                .assetId(stockAsset.getId())
                .assetName(stockAsset.getName())
                .assetType(AssetTypeEnum.STOCK)
                .quantity(quantity)
                .gross(gross)
                .tax(tax)
                .net(net)
                .occurredAt(occurredAt)
                .build();

        when(dtoMapperService.toOperationWithdrawReportDTO(
                eq(w), eq(client),
                eq(quantity), eq(gross),
                eq(tax), eq(net),
                eq(occurredAt)
        )).thenReturn(dto);

        return dto;
    }

    @Test
    @DisplayName("Should list purchases and withdraws without filters")
    void list_noFilters_success() {
        PurchaseModel p = purchase(LocalDate.now().minusDays(3), 10.0, 100.0);
        WithdrawModel w = withdraw(LocalDate.now().minusDays(1), 5.0, 120.0, 10.0, 590.0);

        when(purchaseRepository.findWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(p));
        when(withdrawRepository.findWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(w));

        OperationReportResponseDTO pDto = stubPurchaseDTO(p);
        OperationReportResponseDTO wDto = stubWithdrawDTO(w);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertEquals(2, out.size());
        assertTrue(out.contains(pDto));
        assertTrue(out.contains(wDto));
        verify(adminService).validateAdmin(ADMIN_EMAIL, ADMIN_ACCESS_CODE);
    }

    @Test
    @DisplayName("Should filter only PURCHASE when operationType=PURCHASE")
    void list_filter_operationType_purchase_success() {
        PurchaseModel p = purchase(LocalDate.now().minusDays(2), 3.0, 50.0);
        when(purchaseRepository.findWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(p));
        stubPurchaseDTO(p);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.PURCHASE)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertEquals(1, out.size());
        assertEquals(OperationTypeEnum.PURCHASE, out.get(0).getOperationType());
        verify(purchaseRepository).findWithFilters(null, null, null, null);
        verify(withdrawRepository, never()).findWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should filter only WITHDRAW when operationType=WITHDRAW")
    void list_filter_operationType_withdraw_success() {
        WithdrawModel w = withdraw(LocalDate.now().minusDays(1), 2.0, 80.0, 4.0, 156.0);
        when(withdrawRepository.findWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(w));
        stubWithdrawDTO(w);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .operationType(OperationTypeEnum.WITHDRAW)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertEquals(1, out.size());
        assertEquals(OperationTypeEnum.WITHDRAW, out.get(0).getOperationType());
        verify(withdrawRepository).findWithFilters(null, null, null, null);
        verify(purchaseRepository, never()).findWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should pass assetType filter to repositories")
    void list_filter_assetType_success() {
        PurchaseModel p = purchase(LocalDate.now().minusDays(5), 1.0, 10.0);
        WithdrawModel w = withdraw(LocalDate.now().minusDays(4), 1.0, 12.0, 0.5, 11.5);

        when(purchaseRepository.findWithFilters(isNull(), eq("STOCK"), isNull(), isNull()))
                .thenReturn(List.of(p));
        when(withdrawRepository.findWithFilters(isNull(), eq("STOCK"), isNull(), isNull()))
                .thenReturn(List.of(w));

        stubPurchaseDTO(p);
        stubWithdrawDTO(w);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .assetType(AssetTypeEnum.STOCK)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertEquals(2, out.size());
        verify(purchaseRepository).findWithFilters(null, "STOCK", null, null);
        verify(withdrawRepository).findWithFilters(null, "STOCK", null, null);
    }

    @Test
    @DisplayName("Should filter by clientId: derive walletId for repositories")
    void list_filter_clientId_success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        PurchaseModel p = purchase(LocalDate.now().minusDays(3), 2.0, 100.0);
        WithdrawModel w = withdraw(LocalDate.now().minusDays(1), 2.0, 120.0, 6.0, 234.0);

        when(purchaseRepository.findWithFilters(eq(walletId), isNull(), isNull(), isNull()))
                .thenReturn(List.of(p));
        when(withdrawRepository.findWithFilters(eq(walletId), isNull(), isNull(), isNull()))
                .thenReturn(List.of(w));

        stubPurchaseDTO(p);
        stubWithdrawDTO(w);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(clientId)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertEquals(2, out.size());
        verify(purchaseRepository).findWithFilters(walletId, null, null, null);
        verify(withdrawRepository).findWithFilters(walletId, null, null, null);
    }

    @Test
    @DisplayName("Should return empty when clientId does not exist")
    void list_clientNotFound_returnsEmpty() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(clientId)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertNotNull(out);
        assertTrue(out.isEmpty());
        verify(purchaseRepository, never()).findWithFilters(any(), any(), any(), any());
        verify(withdrawRepository, never()).findWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should pass inclusive date range to repositories")
    void list_filter_dateRange_success() {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now().minusDays(1);

        when(purchaseRepository.findWithFilters(isNull(), isNull(), eq(from), eq(to)))
                .thenReturn(Collections.emptyList());
        when(withdrawRepository.findWithFilters(isNull(), isNull(), eq(from), eq(to)))
                .thenReturn(Collections.emptyList());

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .dateFrom(from)
                .dateTo(to)
                .build();

        List<OperationReportResponseDTO> out = reportService.listOperations(req);

        assertNotNull(out);
        assertEquals(0, out.size());
        verify(purchaseRepository).findWithFilters(null, null, from, to);
        verify(withdrawRepository).findWithFilters(null, null, from, to);
    }

    @Test
    @DisplayName("Should propagate when admin validation fails")
    void list_adminUnauthorized_throws() {
        doThrow(new RuntimeException("Unauthorized"))
                .when(adminService).validateAdmin(ADMIN_EMAIL, ADMIN_ACCESS_CODE);

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .build();

        assertThrows(RuntimeException.class, () -> reportService.listOperations(req));
        verifyNoInteractions(purchaseRepository, withdrawRepository);
    }

    @Test
    @DisplayName("Should forward quantity/gross/tax/net/occurredAt to mapper")
    void list_mapper_values_captured_correctly() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        PurchaseModel p = purchase(LocalDate.now().minusDays(2), 3.0, 50.0);
        WithdrawModel w = withdraw(LocalDate.now().minusDays(1), 2.0, 80.0, 4.0, 156.0);

        when(purchaseRepository.findWithFilters(eq(walletId), isNull(), isNull(), isNull()))
                .thenReturn(List.of(p));
        when(withdrawRepository.findWithFilters(eq(walletId), isNull(), isNull(), isNull()))
                .thenReturn(List.of(w));

        when(dtoMapperService.toOperationPurchaseReportDTO(
                any(), any(), anyDouble(), anyDouble(), any(), any(), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    LocalDateTime occ = inv.getArgument(6, LocalDateTime.class);
                    return OperationReportResponseDTO.builder()
                            .operationType(OperationTypeEnum.PURCHASE)
                            .occurredAt(occ)
                            .build();
                });

        when(dtoMapperService.toOperationWithdrawReportDTO(
                any(), any(), anyDouble(), anyDouble(), any(), any(), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    LocalDateTime occ = inv.getArgument(6, LocalDateTime.class);
                    return OperationReportResponseDTO.builder()
                            .operationType(OperationTypeEnum.WITHDRAW)
                            .occurredAt(occ)
                            .build();
                });

        OperationReportRequestDTO req = OperationReportRequestDTO.builder()
                .adminEmail(ADMIN_EMAIL)
                .adminAccessCode(ADMIN_ACCESS_CODE)
                .clientId(clientId)
                .build();

        reportService.listOperations(req);

        ArgumentCaptor<Double> qtyCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> grossCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> taxCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> netCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<LocalDateTime> occCap = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(dtoMapperService).toOperationPurchaseReportDTO(eq(p), eq(client),
                qtyCap.capture(), grossCap.capture(),
                isNull(), isNull(),
                occCap.capture());
        assertEquals(3.0, qtyCap.getValue(), 1e-6);
        assertEquals(150.0, grossCap.getValue(), 1e-6);
        assertEquals(p.getDate().atStartOfDay(), occCap.getValue());

        verify(dtoMapperService).toOperationWithdrawReportDTO(eq(w), eq(client),
                qtyCap.capture(), grossCap.capture(),
                taxCap.capture(), netCap.capture(),
                occCap.capture());

        List<Double> qAll = qtyCap.getAllValues();
        List<Double> gAll = grossCap.getAllValues();
        List<Double> tAll = taxCap.getAllValues();
        List<Double> nAll = netCap.getAllValues();
        List<LocalDateTime> oAll = occCap.getAllValues();

        assertEquals(2.0, qAll.get(qAll.size() - 1), 1e-6);
        assertEquals(160.0, gAll.get(gAll.size() - 1), 1e-6);
        assertEquals(4.0, tAll.get(tAll.size() - 1), 1e-6);
        assertEquals(156.0, nAll.get(nAll.size() - 1), 1e-6);
        assertEquals(w.getDate().atStartOfDay(), oAll.get(oAll.size() - 1));
    }

    @Test
    @DisplayName("Template.dateOf: returns MIN when model date is null")
    void template_dateOf_nullDate_returnsMin() {
        PurchaseFetcher fetcher = new PurchaseFetcher();

        PurchaseModel p = PurchaseModel.builder()
                .date(null) // <- cobertura do ramo nulo
                .build();

        LocalDateTime out =
                ReflectionTestUtils.invokeMethod(fetcher, "dateOf", p);

        assertNotNull(out);
        assertEquals(LocalDateTime.MIN, out);
    }

    @Test
    @DisplayName("Template.dateOf: returns startOfDay when model date is present")
    void template_dateOf_presentDate_returnsAtStartOfDay() {
        PurchaseFetcher fetcher = new PurchaseFetcher();

        LocalDate d = LocalDate.of(2024, 12, 31);
        PurchaseModel p = PurchaseModel.builder()
                .date(d)
                .build();

        LocalDateTime out =
                ReflectionTestUtils.invokeMethod(fetcher, "dateOf", p);

        assertEquals(d.atStartOfDay(), out);
    }

    @Test
    @DisplayName("Template.walletIdOf: returns null when wallet is null")
    void template_walletIdOf_nullWallet_returnsNull() {
        PurchaseFetcher fetcher = new PurchaseFetcher();

        PurchaseModel p = PurchaseModel.builder()
                .wallet(null)
                .build();

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "walletIdOf", p);

        assertNull(out);
    }

    @Test
    @DisplayName("Template.walletIdOf: returns wallet id when present")
    void template_walletIdOf_walletPresent_returnsId() {
        PurchaseFetcher fetcher = new PurchaseFetcher();

        UUID wid = UUID.randomUUID();
        WalletModel w = WalletModel.builder().id(wid).build();

        PurchaseModel p = PurchaseModel.builder()
                .wallet(w)
                .build();

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "walletIdOf", p);

        assertEquals(wid, out);
    }

    @Test
    @DisplayName("Template.resolveWalletId: returns null when clientId is null")
    void template_resolveWalletId_nullClientId_returnsNull() {
        PurchaseFetcher fetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(fetcher, "clientRepository", clientRepository);

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "resolveWalletId", (UUID) null);

        assertNull(out);
        verifyNoInteractions(clientRepository);
    }

    @Test
    @DisplayName("Template.resolveWalletId: returns null if client exists but has no wallet")
    void template_resolveWalletId_clientWithoutWallet_returnsNull() {
        PurchaseFetcher fetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(fetcher, "clientRepository", clientRepository);

        UUID cid = UUID.randomUUID();
        ClientModel cli = new ClientModel(
                cid, "No Wallet", new EmailModel("nw@test.com"),
                null, null, null, null
        );
        when(clientRepository.findById(cid)).thenReturn(Optional.of(cli));

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "resolveWalletId", cid);

        assertNull(out);
    }

    @Test
    @DisplayName("Template.resolveWalletId: returns null if client wallet id is null")
    void template_resolveWalletId_clientWalletIdNull_returnsNull() {
        PurchaseFetcher fetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(fetcher, "clientRepository", clientRepository);

        UUID cid = UUID.randomUUID();
        WalletModel w = WalletModel.builder().id(null).build();
        ClientModel cli = new ClientModel(
                cid, "Null WalletId", new EmailModel("nwid@test.com"),
                null, null, null, w
        );
        when(clientRepository.findById(cid)).thenReturn(Optional.of(cli));

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "resolveWalletId", cid);

        assertNull(out);
    }

    @Test
    @DisplayName("Template.resolveWalletId: returns wallet id when present")
    void template_resolveWalletId_ok_returnsId() {
        PurchaseFetcher fetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(fetcher, "clientRepository", clientRepository);

        UUID cid = UUID.randomUUID();
        UUID wid = UUID.randomUUID();
        WalletModel w = WalletModel.builder().id(wid).build();
        ClientModel cli = new ClientModel(
                cid, "Alice", new EmailModel("alice@test.com"),
                null, null, null, w
        );
        when(clientRepository.findById(cid)).thenReturn(Optional.of(cli));

        UUID out = ReflectionTestUtils.invokeMethod(fetcher, "resolveWalletId", cid);

        assertEquals(wid, out);
    }

    @Test
    @DisplayName("Template.walletIndex: filters invalid clients and keeps first on key collision")
    void template_walletIndex_filtersAndMerges() {
        PurchaseFetcher fetcher = new PurchaseFetcher();
        ReflectionTestUtils.setField(fetcher, "clientRepository", clientRepository);

        UUID w1 = UUID.randomUUID();

        ClientModel a = new ClientModel(
                UUID.randomUUID(), "A", new EmailModel("a@test.com"),
                null, null, null, WalletModel.builder().id(w1).build()
        );
        ClientModel b = new ClientModel(
                UUID.randomUUID(), "B", new EmailModel("b@test.com"),
                null, null, null, null
        );
        ClientModel c = new ClientModel(
                UUID.randomUUID(), "C", new EmailModel("c@test.com"),
                null, null, null, WalletModel.builder().id(null).build()
        );
        ClientModel d = new ClientModel(
                UUID.randomUUID(), "D", new EmailModel("d@test.com"),
                null, null, null, WalletModel.builder().id(w1).build()
        );

        when(clientRepository.findAll()).thenReturn(Arrays.asList(a, b, c, d));

        Map<UUID, ClientModel> idx =
                ReflectionTestUtils.invokeMethod(fetcher, "walletIndex");

        assertNotNull(idx);
        assertEquals(1, idx.size());
        assertTrue(idx.containsKey(w1));
        assertSame(a, idx.get(w1));
    }

    @Test
    @DisplayName("RequestDTO.isDateRangeValid: true when either date is null")
    void request_isDateRangeValid_eitherNull_true() {
        OperationReportRequestDTO dto1 = OperationReportRequestDTO.builder()
                .dateFrom(null).dateTo(LocalDate.now()).build();
        OperationReportRequestDTO dto2 = OperationReportRequestDTO.builder()
                .dateFrom(LocalDate.now()).dateTo(null).build();
        OperationReportRequestDTO dto3 = OperationReportRequestDTO.builder()
                .dateFrom(null).dateTo(null).build();

        assertTrue(dto1.isDateRangeValid());
        assertTrue(dto2.isDateRangeValid());
        assertTrue(dto3.isDateRangeValid());
    }

    @Test
    @DisplayName("RequestDTO.isDateRangeValid: false when from is after to")
    void request_isDateRangeValid_fromAfterTo_false() {
        OperationReportRequestDTO dto = OperationReportRequestDTO.builder()
                .dateFrom(LocalDate.now().plusDays(1))
                .dateTo(LocalDate.now())
                .build();
        assertFalse(dto.isDateRangeValid());
    }

    @Test
    @DisplayName("RequestDTO.isDateRangeValid: true when from equals to")
    void request_isDateRangeValid_fromEqualsTo_true() {
        LocalDate d = LocalDate.of(2025, 1, 1);
        OperationReportRequestDTO dto = OperationReportRequestDTO.builder()
                .dateFrom(d).dateTo(d).build();
        assertTrue(dto.isDateRangeValid());
    }
}
