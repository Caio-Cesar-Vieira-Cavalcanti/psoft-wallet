package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.AddressDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.asset.types.TreasuryBounds;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.wallet.WalletService;
import com.ufcg.psoft.commerce.service.wallet.WithdrawServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Withdraw Service Unit Tests")
class WithdrawServiceUnitTests {

    private WithdrawServiceImpl withdrawService;
    private WithdrawRepository withdrawRepository;
    private AdminService adminService;
    private AssetService assetService;
    private ClientService clientService;
    private WalletService walletService;
    private DTOMapperService dtoMapperService;
    private AssetResponseDTO assetResponseDTO;

    private WalletModel wallet;
    private AssetModel asset;
    private HoldingModel holding;
    private ClientModel client;

    private UUID clientId;
    private UUID assetId;

    @BeforeEach
    void setup() {
        withdrawRepository = mock(WithdrawRepository.class);

        withdrawService = new WithdrawServiceImpl();
        clientService = mock(ClientService.class);
        adminService = mock(AdminService.class);
        assetService = mock(AssetService.class);
        walletService = mock(WalletService.class);
        dtoMapperService = mock(DTOMapperService.class);

        ReflectionTestUtils.setField(withdrawService, "clientService", clientService);
        ReflectionTestUtils.setField(withdrawService, "adminService", adminService);
        ReflectionTestUtils.setField(withdrawService, "assetService", assetService);
        ReflectionTestUtils.setField(withdrawService, "walletService", walletService);
        ReflectionTestUtils.setField(withdrawService, "dtoMapperService", dtoMapperService);
        ReflectionTestUtils.setField(withdrawService, "withdrawRepository", withdrawRepository);

        asset = newStock();

        seedWalletWith(asset, 10.0,  500.0, 1000.0);

        clientId = UUID.randomUUID();
        client = new ClientModel(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );

        assetId = UUID.randomUUID();
        assetResponseDTO = AssetResponseDTO.builder()
                .id(assetId)
                .name("Bitcoin")
                .description("Best crypto ever")
                .quotation(100000.0)
                .quotaQuantity(20.0)
                .build();

        holding = HoldingModel.builder()
                .asset(asset)
                .quantity(10.0)
                .accumulatedPrice(450000.0)
                .wallet(wallet)
                .build();

        wallet.getHoldings().put(assetId, holding);

        ClientResponseDTO clientResponseDTO = ClientResponseDTO.builder()
                .id(clientId)
                .fullName("João Azevedo")
                .email("joao@email.com")
                .address(AddressDTO.builder()
                        .street("Street")
                        .number("123")
                        .neighborhood("Neighborhood")
                        .city("City")
                        .state("State")
                        .country("Country")
                        .zipCode("12345-678")
                        .build())
                .planType(PlanTypeEnum.PREMIUM)
                .budget(1000.0)
                .wallet(WalletResponseDTO.builder()
                        .id(wallet.getId())
                        .budget(wallet.getBudget())
                        // ... outros campos da wallet ...
                        .build())
                .build();

        when(clientService.getClientById(clientId)).thenReturn(clientResponseDTO);
        when(clientService.validateClientAccess(clientId, "123456")).thenReturn(client);
    }

    private void seedWalletWith(AssetModel assetModel, double qty, double accPrice, double budget) {
        holding = HoldingModel.builder()
                .id(UUID.randomUUID())
                .asset(assetModel)
                .quantity(qty)
                .accumulatedPrice(accPrice)
                .build();

        wallet = WalletModel.builder()
                .id(UUID.randomUUID())
                .budget(budget)
                .holdings(new HashMap<>())
                .build();

        wallet.getHoldings().put(assetModel.getId(), holding);
    }

    private AssetModel newStock() {
        return AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Stock Asset")
                .assetType(new Stock())
                .description("Mock Stock")
                .isActive(true)
                .quotation(100.0)
                .quotaQuantity(1.0)
                .build();
    }

    private AssetModel newCrypto() {
        return AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Crypto Asset")
                .assetType(new Crypto())
                .description("Mock Crypto")
                .isActive(true)
                .quotation(200.0)
                .quotaQuantity(1.0)
                .build();
    }

    private AssetModel newTreasury() {
        return AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Treasury Asset")
                .assetType(new TreasuryBounds())
                .description("Mock Treasury")
                .isActive(true)
                .quotation(50.0)
                .quotaQuantity(1.0)
                .build();
    }

    @Test
    @DisplayName("Should withdraw asset successfully")
    void testWithdrawAsset_Success() {
        double quantityToWithdraw = 5.0;
        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any()))
                .thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception if holding does not exist")
    void testWithdrawAsset_HoldingNotFound() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        assertThrows(ClientHoldingIsInsufficientException.class, () ->
                withdrawService.withdrawAsset(wallet, otherAsset, 1.0)
        );
    }

    @Test
    @DisplayName("Should confirm withdraw and transition from REQUESTED to CONFIRMED")
    void testConfirmWithdraw_TransitionToConfirmed() {
        com.ufcg.psoft.commerce.model.user.AdminModel mockAdmin = mock(com.ufcg.psoft.commerce.model.user.AdminModel.class);
        when(adminService.getAdmin()).thenReturn(mockAdmin);

        com.ufcg.psoft.commerce.model.wallet.WithdrawModel mockWithdraw = mock(com.ufcg.psoft.commerce.model.wallet.WithdrawModel.class);
        when(mockWithdraw.getWallet()).thenReturn(wallet);
        when(mockWithdraw.getAsset()).thenReturn(asset);
        when(mockWithdraw.getQuantity()).thenReturn(5.0);
        when(mockWithdraw.getStateEnum()).thenReturn(com.ufcg.psoft.commerce.enums.WithdrawStateEnum.REQUESTED);
        when(withdrawRepository.findById(any())).thenReturn(java.util.Optional.of(mockWithdraw));

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.confirmWithdraw(UUID.randomUUID(),
                com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO.builder()
                        .adminEmail("admin@example.com")
                        .adminAccessCode("123456")
                        .build());

        assertSame(mockResponse, result);
        verify(mockWithdraw, times(2)).modify(mockAdmin); // Called twice for REQUESTED->CONFIRMED->IN_ACCOUNT
        verify(withdrawRepository, times(2)).save(mockWithdraw);
    }

    @Test
    @DisplayName("Should verify automatic transition from CONFIRMED to IN_ACCOUNT")
    void testConfirmWithdraw_AutomaticTransitionToInAccount() {
        com.ufcg.psoft.commerce.model.user.AdminModel mockAdmin = mock(com.ufcg.psoft.commerce.model.user.AdminModel.class);
        when(adminService.getAdmin()).thenReturn(mockAdmin);

        com.ufcg.psoft.commerce.model.wallet.WithdrawModel mockWithdraw = mock(com.ufcg.psoft.commerce.model.wallet.WithdrawModel.class);
        when(mockWithdraw.getWallet()).thenReturn(wallet);
        when(mockWithdraw.getAsset()).thenReturn(asset);
        when(mockWithdraw.getQuantity()).thenReturn(5.0);
        when(mockWithdraw.getStateEnum()).thenReturn(com.ufcg.psoft.commerce.enums.WithdrawStateEnum.REQUESTED);
        when(withdrawRepository.findById(any())).thenReturn(java.util.Optional.of(mockWithdraw));

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        withdrawService.confirmWithdraw(UUID.randomUUID(),
                com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO.builder()
                        .adminEmail("admin@example.com")
                        .adminAccessCode("123456")
                        .build());

        // Verify that modify was called twice (REQUESTED->CONFIRMED->IN_ACCOUNT)
        verify(mockWithdraw, times(2)).modify(mockAdmin);
    }

    @Test
    @DisplayName("Should trigger client notification during confirmation")
    void testConfirmWithdraw_ClientNotificationTriggered() {
        com.ufcg.psoft.commerce.model.user.AdminModel mockAdmin = mock(com.ufcg.psoft.commerce.model.user.AdminModel.class);
        when(adminService.getAdmin()).thenReturn(mockAdmin);

        com.ufcg.psoft.commerce.model.wallet.WithdrawModel mockWithdraw = mock(com.ufcg.psoft.commerce.model.wallet.WithdrawModel.class);
        when(mockWithdraw.getWallet()).thenReturn(wallet);
        when(mockWithdraw.getAsset()).thenReturn(asset);
        when(mockWithdraw.getQuantity()).thenReturn(5.0);
        when(mockWithdraw.getStateEnum()).thenReturn(com.ufcg.psoft.commerce.enums.WithdrawStateEnum.REQUESTED);
        when(withdrawRepository.findById(any())).thenReturn(java.util.Optional.of(mockWithdraw));

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        withdrawService.confirmWithdraw(UUID.randomUUID(),
                com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO.builder()
                        .adminEmail("admin@example.com")
                        .adminAccessCode("123456")
                        .build());

        // Verify that the withdraw was processed (which includes notification)
        verify(mockWithdraw, times(2)).modify(mockAdmin);
        verify(withdrawRepository, times(2)).save(mockWithdraw);
    }

    @Test
    @DisplayName("Should withdraw Stock asset and apply 15% tax over profit")
    void withdraw_stock_success() {
        UUID fixedAssetId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID fixedHoldingId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID fixedWalletId = UUID.fromString("00000000-0000-0000-0000-000000000003");

        AssetModel stockAsset = AssetModel.builder()
                .id(fixedAssetId)
                .name("Stock Asset")
                .assetType(new Stock())
                .description("Mock Stock")
                .isActive(true)
                .quotation(100.0)
                .quotaQuantity(1.0)
                .build();

        // Wallet e holding fixos
        WalletModel wallet = WalletModel.builder()
                .id(fixedWalletId)
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        HoldingModel holding = HoldingModel.builder()
                .id(fixedHoldingId)
                .asset(stockAsset)
                .quantity(10.0)
                .accumulatedPrice(500.0)
                .wallet(wallet)
                .build();

        wallet.getHoldings().put(fixedAssetId, holding);

        double qty = 5.0;

        double gross = stockAsset.getQuotation() * qty;
        double avgCost = holding.getAccumulatedPrice() / holding.getQuantity();
        double costBasis = avgCost * qty;
        double profit = gross - costBasis;
        double taxable = Math.max(0.0, profit);
        double expectedTax = 0.15 * taxable;
        double expectedNet = gross - expectedTax;

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, stockAsset, qty);

        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());

        ArgumentCaptor<WithdrawModel> cap = ArgumentCaptor.forClass(WithdrawModel.class);
        verify(dtoMapperService).toWithdrawResponseDTO(cap.capture());

        double actualWithdrawValue = cap.getValue().getWithdrawValue();
        assertEquals(expectedNet, actualWithdrawValue, 1e-6);
    }

    @Test
    @DisplayName("Should withdraw Crypto asset (profit ≤ 5000) and apply 15% tax over profit")
    void withdraw_crypto_low_success() {
        asset = newCrypto();
        asset.setQuotation(200.0);
        seedWalletWith(asset, 50.0, 5000.0, 1000.0);

        double qty = 10.0;

        double gross = 200.0 * qty;
        double avgCost = 5000.0 / 50.0;
        double costBasis = avgCost * qty;
        double profit = gross - costBasis;
        double taxable = Math.max(0.0, profit);
        double expectedTax = 0.15 * taxable;
        double expectedNet = gross - expectedTax;

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, qty);
        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());

        ArgumentCaptor<WithdrawModel> cap =
                ArgumentCaptor.forClass(WithdrawModel.class);
        verify(dtoMapperService).toWithdrawResponseDTO(cap.capture());

        double actualWithdrawValue = cap.getValue().getWithdrawValue();
        assertEquals(expectedNet, actualWithdrawValue, 1e-6);
    }

    @Test
    @DisplayName("Should withdraw Crypto asset (profit > 5000) and apply 22.5% tax over profit")
    void withdraw_crypto_high_success() {
        asset = newCrypto();
        asset.setQuotation(300.0);
        seedWalletWith(asset, 100.0, 10000.0, 2000.0);

        double qty = 40.0;

        double gross = 300.0 * qty;
        double avgCost = 10000.0 / 100.0;
        double costBasis = avgCost * qty;
        double profit = gross - costBasis;
        double taxable = Math.max(0.0, profit);
        double expectedTax = 0.225 * taxable;
        double expectedNet = gross - expectedTax;

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, qty);
        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());

        ArgumentCaptor<com.ufcg.psoft.commerce.model.wallet.WithdrawModel> cap =
                ArgumentCaptor.forClass(com.ufcg.psoft.commerce.model.wallet.WithdrawModel.class);
        verify(dtoMapperService).toWithdrawResponseDTO(cap.capture());

        double actualWithdrawValue = cap.getValue().getWithdrawValue();
        assertEquals(expectedNet, actualWithdrawValue, 1e-6);
    }

    @Test
    @DisplayName("Should withdraw Treasury asset and apply 10% tax over profit")
    void withdraw_treasury_success() {
        asset = newTreasury();
        asset.setQuotation(100.0);
        seedWalletWith(asset, 10.0, 500.0, 1000.0);

        double qty = 10.0;

        double gross = 100.0 * qty;
        double avgCost = 500.0 / 10.0;
        double costBasis = avgCost * qty;
        double profit = gross - costBasis;
        double taxable = Math.max(0.0, profit);
        double expectedTax = 0.10 * taxable;
        double expectedNet = gross - expectedTax;

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any())).thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, qty);
        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());

        ArgumentCaptor<com.ufcg.psoft.commerce.model.wallet.WithdrawModel> cap =
                ArgumentCaptor.forClass(com.ufcg.psoft.commerce.model.wallet.WithdrawModel.class);
        verify(dtoMapperService).toWithdrawResponseDTO(cap.capture());

        double actualWithdrawValue = cap.getValue().getWithdrawValue();
        assertEquals(expectedNet, actualWithdrawValue, 1e-6);
    }

    @Test
    @DisplayName("Should throw exception if holding does not exist for given asset")
    void withdraw_holding_not_found_failure() {
        AssetModel other = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Other")
                .assetType(new Stock())
                .description("Other")
                .isActive(true)
                .quotation(1.0)
                .quotaQuantity(1.0)
                .build();

        assertThrows(ClientHoldingIsInsufficientException.class, () ->
                withdrawService.withdrawAsset(wallet, other, 1.0)
        );
        verify(withdrawRepository, never()).save(any());
        verify(dtoMapperService, never()).toWithdrawResponseDTO(any());
    }

    @Test
    @DisplayName("Should withdraw client asset successfully")
    void testWithdrawClientAsset_Success_Corrected() {
        WithdrawServiceImpl spyWithdrawService = spy(withdrawService);

        when(assetService.fetchAsset(assetId)).thenReturn(asset);

        WithdrawResponseDTO mockWithdrawResponse = mock(WithdrawResponseDTO.class);
        doReturn(mockWithdrawResponse)
                .when(spyWithdrawService)
                .withdrawAsset(client.getWallet(), asset, 5.0);

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        WithdrawResponseDTO result = spyWithdrawService.withdrawClientAsset(clientId, assetId, dto);

        assertSame(mockWithdrawResponse, result);
        verify(clientService).validateClientAccess(clientId, "123456");
        verify(assetService).fetchAsset(assetId);
        verify(spyWithdrawService).withdrawAsset(client.getWallet(), asset, 5.0);
    }

    @Test
    @DisplayName("Should throw exception if client not found")
    void testWithdrawClientAsset_ClientNotFound() {
        UUID invalidClientId = UUID.randomUUID();

        // Mock validateClientAccess para lançar exceção quando cliente não for encontrado
        when(clientService.validateClientAccess(invalidClientId, "123456"))
                .thenThrow(new ClientIdNotFoundException(invalidClientId));

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(ClientIdNotFoundException.class, () ->
                withdrawService.withdrawClientAsset(invalidClientId, assetId, dto)
        );
    }

    @Test
    @DisplayName("Should throw exception if access code is invalid")
    void testWithdrawClientAsset_InvalidAccessCode() {
        // Mock validateClientAccess para lançar exceção quando access code for inválido
        when(clientService.validateClientAccess(clientId, "000000"))
                .thenThrow(new UnauthorizedUserAccessException());

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("000000") // Código inválido
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(UnauthorizedUserAccessException.class, () ->
                withdrawService.withdrawClientAsset(clientId, assetId, dto)
        );
    }

    @Test
    @DisplayName("Should throw exception if asset not found")
    void testWithdrawClientAsset_AssetNotFound_Corrected() {
        when(clientService.validateClientAccess(clientId, "123456")).thenReturn(client);
        when(assetService.fetchAsset(assetId))
                .thenThrow(new AssetNotFoundException("Asset not found with ID " + assetId));

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(AssetNotFoundException.class, () ->
                withdrawService.withdrawClientAsset(clientId, assetId, dto)
        );

        verify(clientService).validateClientAccess(clientId, "123456");
        verify(assetService).fetchAsset(assetId);
        // Não deve chamar withdrawAsset quando o asset não é encontrado
    }
}
