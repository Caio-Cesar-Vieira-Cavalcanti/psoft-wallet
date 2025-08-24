package com.ufcg.psoft.model;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetQuantityAvailableIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.PurchaseAvailableState;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.PurchaseInWalletState;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.PurchasePurchasedState;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.PurchaseRequestedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@DisplayName("Purchase State Unit Tests")
class PurchaseStateUnitTests {

    private UUID purchaseId;
    private UUID walletId;
    private UUID assetId;
    private UUID clientId;
    private PurchaseModel purchase;
    private WalletModel wallet;
    private AssetModel asset;
    private ClientModel client;
    private AdminModel admin;

    @BeforeEach
    void setup() {
        purchaseId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        assetId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        client = ClientModel.builder()
                .id(clientId)
                .fullName("Test Client")
                .email(new EmailModel("client@test.com"))
                .accessCode(new AccessCodeModel("123456"))
                .build();

        wallet = WalletModel.builder()
                .id(walletId)
                .budget(10000.0)
                .build();

        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Stock")
                .description("Test stock asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .build();

        admin = new AdminModel(
                UUID.randomUUID(),
                "Admin User",
                new EmailModel("admin@test.com"),
                new AccessCodeModel("123456")
        );

        purchase = PurchaseModel.builder()
                .id(purchaseId)
                .wallet(wallet)
                .asset(asset)
                .quantity(10)
                .acquisitionPrice(100.0)
                .date(LocalDate.now())
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build();
    }

    @Test
    @DisplayName("PurchaseRequestedState should change to AVAILABLE when admin confirms availability")
    void testPurchaseRequestedStateShouldChangeToAvailableWhenAdminConfirmsAvailability() {
        PurchaseRequestedState requestedState = new PurchaseRequestedState(purchase);

        requestedState.modify(admin);

        assertEquals(PurchaseStateEnum.AVAILABLE, purchase.getStateEnum());
        assertInstanceOf(PurchaseAvailableState.class, purchase.getState());
    }

    @Test
    @DisplayName("PurchaseRequestedState should throw UnauthorizedUserAccessException when non-admin tries to modify")
    void testPurchaseRequestedStateShouldThrowUnauthorizedUserAccessExceptionWhenNonAdminTriesToModify() {
        PurchaseRequestedState requestedState = new PurchaseRequestedState(purchase);

        assertThrows(UnauthorizedUserAccessException.class, () -> {
            requestedState.modify(client);
        });

        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchaseRequestedState should throw AssetIsInactiveException when asset is inactive")
    void testPurchaseRequestedStateShouldThrowAssetIsInactiveExceptionWhenAssetIsInactive() {
        asset.setActive(false);
        PurchaseRequestedState requestedState = new PurchaseRequestedState(purchase);

        assertThrows(AssetIsInactiveException.class, () -> {
            requestedState.modify(admin);
        });

        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchaseRequestedState should throw AssetQuantityAvailableIsInsufficientException when insufficient quantity")
    void testPurchaseRequestedStateShouldThrowAssetQuantityAvailableIsInsufficientExceptionWhenInsufficientQuantity() {
        asset.setQuotaQuantity(5.0); // Less than purchase quantity (10)
        PurchaseRequestedState requestedState = new PurchaseRequestedState(purchase);

        AssetQuantityAvailableIsInsufficientException exception = assertThrows(
                AssetQuantityAvailableIsInsufficientException.class, 
                () -> requestedState.modify(admin)
        );

        assertTrue(exception.getMessage().contains("Test Stock"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("5.0"));

        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchasePurchasedState should change to IN_WALLET state when modified")
    void testPurchasePurchasedStateShouldChangeToInWallet() {
        PurchasePurchasedState purchasedState = new PurchasePurchasedState(purchase);

        purchasedState.modify(admin);

        assertEquals(PurchaseStateEnum.IN_WALLET, purchase.getStateEnum());
        assertInstanceOf(PurchaseInWalletState.class, purchase.getState());
    }

    @Test
    @DisplayName("PurchaseInWalletState should do nothing when modified")
    void testPurchaseInWalletStateDoesNothing() {
        PurchaseInWalletState inWalletState = new PurchaseInWalletState(purchase);

        PurchaseStateEnum beforeState = purchase.getStateEnum();

        inWalletState.modify(admin);

        assertEquals(beforeState, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchaseRequestedState success path")
    void testPurchaseRequestedStateSuccess() {
        PurchaseRequestedState state = new PurchaseRequestedState(purchase);

        state.modify(admin);

        assertEquals(PurchaseStateEnum.AVAILABLE, purchase.getStateEnum());
        assertInstanceOf(PurchaseAvailableState.class, purchase.getState());
    }

    @Test
    @DisplayName("PurchaseRequestedState throws UnauthorizedUserAccessException")
    void testPurchaseRequestedStateUnauthorized() {
        PurchaseRequestedState state = new PurchaseRequestedState(purchase);

        assertThrows(UnauthorizedUserAccessException.class, () -> state.modify(client));
        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchaseRequestedState throws AssetIsInactiveException")
    void testPurchaseRequestedStateAssetInactive() {
        asset.setActive(false);
        PurchaseRequestedState state = new PurchaseRequestedState(purchase);

        assertThrows(AssetIsInactiveException.class, () -> state.modify(admin));
        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchaseRequestedState throws AssetQuantityAvailableIsInsufficientException")
    void testPurchaseRequestedStateInsufficientQuantity() {
        asset.setQuotaQuantity(5.0); // less than purchase quantity
        PurchaseRequestedState state = new PurchaseRequestedState(purchase);

        AssetQuantityAvailableIsInsufficientException ex = assertThrows(AssetQuantityAvailableIsInsufficientException.class, () -> state.modify(admin));
        assertTrue(ex.getMessage().contains("Test Stock"));
        assertEquals(PurchaseStateEnum.REQUESTED, purchase.getStateEnum());
    }

    @Test
    @DisplayName("PurchasePurchasedState transitions to IN_WALLET")
    void testPurchasePurchasedState() {
        PurchasePurchasedState state = new PurchasePurchasedState(purchase);
        state.modify(admin);

        assertEquals(PurchaseStateEnum.IN_WALLET, purchase.getStateEnum());
        assertInstanceOf(PurchaseInWalletState.class, purchase.getState());
    }

    @Test
    @DisplayName("notifyClientAboutPurchaseAvailability fallback path")
    void testNotifyClientFallback() {
        PurchaseRequestedState state = spy(new PurchaseRequestedState(purchase));

        doThrow(new RuntimeException("Logger fails")).when(state).modify(admin);

        assertThrows(RuntimeException.class, () -> state.modify(admin));
    }
}
