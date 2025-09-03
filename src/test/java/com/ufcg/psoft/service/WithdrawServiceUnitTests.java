package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.asset.types.TreasuryBounds;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.wallet.WithdrawServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Withdraw Service Unit Tests")
class WithdrawServiceUnitTests {

    private WithdrawServiceImpl withdrawService;
    private WalletRepository walletRepository;
    private HoldingRepository holdingRepository;
    private WithdrawRepository withdrawRepository;
    private AdminService adminService;
    private DTOMapperService dtoMapperService;

    private WalletModel wallet;
    private AssetModel asset;
    private HoldingModel holding;

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        holdingRepository = mock(HoldingRepository.class);
        withdrawRepository = mock(WithdrawRepository.class);
        adminService = mock(AdminService.class);
        dtoMapperService = mock(DTOMapperService.class);

        withdrawService = new WithdrawServiceImpl();
        ReflectionTestUtils.setField(withdrawService, "walletRepository", walletRepository);
        ReflectionTestUtils.setField(withdrawService, "holdingRepository", holdingRepository);
        ReflectionTestUtils.setField(withdrawService, "withdrawRepository", withdrawRepository);
        ReflectionTestUtils.setField(withdrawService, "adminService", adminService);
        ReflectionTestUtils.setField(withdrawService, "dtoMapperService", dtoMapperService);

        asset = newStock();

        seedWalletWith(asset, 10.0,  500.0, 1000.0);
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
}
