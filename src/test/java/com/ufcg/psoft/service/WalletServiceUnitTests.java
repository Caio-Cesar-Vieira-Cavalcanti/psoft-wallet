package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.wallet.WalletService;
import com.ufcg.psoft.commerce.service.wallet.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Wallet Service Unit Tests")
class WalletServiceUnitTests {

    private WalletRepository walletRepository;
    private HoldingRepository holdingRepository;
    private PurchaseRepository purchaseRepository;
    private WalletService walletService;

    private WalletModel wallet;
    private AssetModel asset;
    private PurchaseModel purchase;
    private PurchaseResponseDTO purchaseResponse;
    private PurchaseResponseAfterAddedInWalletDTO walletPurchaseResponse;

    private UUID walletId;

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        purchaseRepository = mock(PurchaseRepository.class);
        holdingRepository = mock(HoldingRepository.class);

        walletService = new WalletServiceImpl();

        ReflectionTestUtils.setField(walletService, "walletRepository", walletRepository);
        ReflectionTestUtils.setField(walletService, "purchaseRepository", purchaseRepository);
        ReflectionTestUtils.setField(walletService, "holdingRepository", holdingRepository);

        walletId = UUID.randomUUID();

        wallet = WalletModel.builder()
                .id(walletId)
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        asset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Mock Asset")
                .quotation(100.0)
                .build();

        purchase = mock(PurchaseModel.class);
        when(purchase.getWallet()).thenReturn(wallet);
        when(purchase.getAsset()).thenReturn(asset);
        when(purchase.getQuantity()).thenReturn(5.0);
        when(purchase.getStateEnum()).thenReturn(PurchaseStateEnum.REQUESTED);
        when(purchase.getDate()).thenReturn(LocalDate.now());
        when(purchase.getId()).thenReturn(UUID.randomUUID());

        purchaseResponse = PurchaseResponseDTO.builder()
                .walletId(wallet.getId())
                .assetId(asset.getId())
                .quantity(5.0)
                .purchaseState(PurchaseStateEnum.REQUESTED)
                .date(LocalDate.now())
                .build();

        HoldingModel holding = HoldingModel.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .quantity(purchase.getQuantity())
                .build();

        walletPurchaseResponse = new PurchaseResponseAfterAddedInWalletDTO(purchase, holding);
    }

    @Test
    @DisplayName("Should find holding by asset when holding exists")
    void testFindHoldingByAsset_Existing() {
        HoldingModel holding = mock(HoldingModel.class);
        when(holding.getAsset()).thenReturn(asset);
        wallet.getHoldings().put(asset.getId(), holding);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertSame(holding, result);
    }

    @Test
    @DisplayName("Should return null when holdings are empty")
    void testFindHoldingByAsset_EmptyHoldings() {
        wallet.setHoldings(new HashMap<>());

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when holdings map is null")
    void testFindHoldingByAsset_NullHoldings() {
        wallet.setHoldings(null);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should add purchase when holding does not exist")
    void testAddPurchase_NewHolding() {
        when(walletService.addedInWallet(purchase, null)).thenReturn(walletPurchaseResponse);

        PurchaseResponseDTO result = walletService.addPurchase(purchase);

        assertEquals(purchaseResponse.getQuantity(), result.getQuantity());
        assertEquals(purchaseResponse.getPurchaseState(), result.getPurchaseState());
        assertEquals(purchaseResponse.getDate(), result.getDate());
        assertEquals(purchaseResponse.getWalletId(), result.getWalletId());
        assertEquals(purchaseResponse.getAssetId(), result.getAssetId());
        verify(walletService).addedInWallet(purchase, null);
    }

    @Test
    @DisplayName("Should add purchase when holding exists")
    void testAddPurchase_ExistingHolding() {
        HoldingModel holding = mock(HoldingModel.class);
        when(holding.getAsset()).thenReturn(asset);
        wallet.getHoldings().put(asset.getId(), holding);

        when(walletService.addedInWallet(purchase, holding)).thenReturn(walletPurchaseResponse);

        PurchaseResponseDTO result = walletService.addPurchase(purchase);

        assertEquals(purchaseResponse.getQuantity(), result.getQuantity());
        assertEquals(purchaseResponse.getPurchaseState(), result.getPurchaseState());
        assertEquals(purchaseResponse.getDate(), result.getDate());
        assertEquals(purchaseResponse.getWalletId(), result.getWalletId());
        assertEquals(purchaseResponse.getAssetId(), result.getAssetId());

        verify(walletService).addedInWallet(purchase, holding);
    }

    @Test
    @DisplayName("Should handle multiple holdings and return correct one")
    void testFindHoldingByAsset_MultipleHoldings() {
        HoldingModel holding1 = mock(HoldingModel.class);
        when(holding1.getAsset()).thenReturn(asset);
        HoldingModel holding2 = mock(HoldingModel.class);
        when(holding2.getAsset()).thenReturn(asset);
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        wallet.getHoldings().put(otherAsset.getId(), holding1);
        wallet.getHoldings().put(asset.getId(), holding2);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertEquals(asset, result.getAsset());
    }

    @Test
    @DisplayName("Should return null when no holding matches the asset")
    void testFindHoldingByAsset_NoMatch() {
        HoldingModel holding = mock(HoldingModel.class);
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        when(holding.getAsset()).thenReturn(otherAsset);
        wallet.getHoldings().put(otherAsset.getId(), holding);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }
}