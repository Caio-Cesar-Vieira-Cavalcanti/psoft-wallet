package com.ufcg.psoft.model;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawConfirmedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawInAccountState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawRequestedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Withdraw Model Unit Tests")
class WithdrawModelUnitTests {

    private WithdrawModel withdraw;
    private UserModel adminUser;
    private AssetModel asset;
    private WalletModel wallet;

    @BeforeEach
    void setup() {
        adminUser = new AdminModel(
            UUID.randomUUID(),
            "Admin User",
            new EmailModel("admin@test.com"),
            new AccessCodeModel("123456")
        );

        Stock stockType = new Stock();

        asset = AssetModel.builder()
            .id(UUID.randomUUID())
            .name("Test Asset")
            .assetType(stockType)
            .description("Test Asset Description")
            .isActive(true)
            .quotation(100.0)
            .quotaQuantity(1.0)
            .build();

        wallet = WalletModel.builder()
            .id(UUID.randomUUID())
            .budget(1000.0)
            .holdings(new HashMap<>())
            .build();

        withdraw = WithdrawModel.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .wallet(wallet)
                .quantity(10.0)
                .date(LocalDate.now())
                .sellingPrice(500.0)
                .tax(50.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();
    }

    @Test
    @DisplayName("Should create WithdrawModel with correct values")
    void testWithdrawModelCreation() {
        assertEquals(500.0, withdraw.getSellingPrice());
        assertEquals(50.0, withdraw.getTax());
        assertEquals(WithdrawStateEnum.REQUESTED, withdraw.getStateEnum());
        assertEquals(asset, withdraw.getAsset());
        assertEquals(wallet, withdraw.getWallet());
        assertEquals(10.0, withdraw.getQuantity());
        assertNotNull(withdraw.getDate());
        assertNull(withdraw.getState()); // Antes do loadState
    }

    @Test
    @DisplayName("Should load state correctly based on stateEnum")
    void testLoadState() {
        withdraw.loadState();

        assertNotNull(withdraw.getState());
        assertInstanceOf(WithdrawRequestedState.class, withdraw.getState());

        withdraw.setStateEnum(WithdrawStateEnum.CONFIRMED);
        withdraw.loadState();
        assertInstanceOf(WithdrawConfirmedState.class, withdraw.getState());

        withdraw.setStateEnum(WithdrawStateEnum.IN_ACCOUNT);
        withdraw.loadState();
        assertInstanceOf(WithdrawInAccountState.class, withdraw.getState());
    }

    @Test
    @DisplayName("Should set state and enum correctly using setState")
    void testSetState() {
        WithdrawState confirmedState = new WithdrawConfirmedState(withdraw);
        withdraw.setState(confirmedState, WithdrawStateEnum.CONFIRMED);

        assertEquals(WithdrawStateEnum.CONFIRMED, withdraw.getStateEnum());
        assertSame(confirmedState, withdraw.getState());
    }

    @Test
    @DisplayName("Should update selling price correctly")
    void testSetSellingPrice() {
        withdraw.setSellingPrice(750.0);
        assertEquals(750.0, withdraw.getSellingPrice());
    }

    @Test
    @DisplayName("Should update tax correctly")
    void testSetTax() {
        withdraw.setTax(75.0);
        assertEquals(75.0, withdraw.getTax());
    }

    @Test
    @DisplayName("WithdrawRequestedState should change to CONFIRMED when modified by admin")
    void testWithdrawRequestedStateModify() {
        WithdrawRequestedState requestedState = new WithdrawRequestedState(withdraw);

        assertThrows(Exception.class, () -> {
            requestedState.modify(adminUser);
        });
    }

    @Test
    @DisplayName("WithdrawConfirmedState should change to IN_ACCOUNT")
    void testWithdrawConfirmedStateModify() {
        withdraw.setState(new WithdrawConfirmedState(withdraw), WithdrawStateEnum.CONFIRMED);
        WithdrawConfirmedState confirmedState = new WithdrawConfirmedState(withdraw);

        confirmedState.modify(adminUser);

        assertEquals(WithdrawStateEnum.IN_ACCOUNT, withdraw.getStateEnum());
        assertInstanceOf(WithdrawInAccountState.class, withdraw.getState());
    }

    @Test
    @DisplayName("WithdrawInAccountState should do nothing when modified")
    void testWithdrawInAccountStateDoesNothing() {
        withdraw.setState(new WithdrawInAccountState(withdraw), WithdrawStateEnum.IN_ACCOUNT);
        WithdrawInAccountState inAccountState = new WithdrawInAccountState(withdraw);

        WithdrawStateEnum beforeState = withdraw.getStateEnum();

        inAccountState.modify(adminUser);

        assertEquals(beforeState, withdraw.getStateEnum());
        assertInstanceOf(WithdrawInAccountState.class, withdraw.getState());
    }

    @Test
    @DisplayName("Should call modify method on WithdrawModel correctly")
    void testWithdrawModelModify() {
        withdraw.loadState();

        assertThrows(Exception.class, () -> {
            withdraw.modify(adminUser);
        });
    }
}
