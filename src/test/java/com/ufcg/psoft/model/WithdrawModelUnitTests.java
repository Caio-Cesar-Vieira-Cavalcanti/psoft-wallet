package com.ufcg.psoft.model;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawConfirmedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawInAccountState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawRequestedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Withdraw Model Unit Tests")
class WithdrawModelUnitTests {

    private WithdrawModel withdraw;

    @BeforeEach
    void setup() {
        withdraw = WithdrawModel.builder()
                .sellingPrice(500.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();
    }

    @Test
    @DisplayName("Should create WithdrawModel with correct values")
    void testWithdrawModelCreation() {
        assertEquals(500.0, withdraw.getSellingPrice());
        assertEquals(WithdrawStateEnum.REQUESTED, withdraw.getStateEnum());
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
    @DisplayName("WithdrawRequestedState should change to CONFIRMED")
    void testWithdrawRequestedStateModify() {
        WithdrawRequestedState requestedState = new WithdrawRequestedState(withdraw);

        requestedState.modify();

        assertEquals(WithdrawStateEnum.CONFIRMED, withdraw.getStateEnum());
        assertInstanceOf(WithdrawConfirmedState.class, withdraw.getState());
    }

    @Test
    @DisplayName("WithdrawConfirmedState should change to IN_ACCOUNT")
    void testWithdrawConfirmedStateModify() {
        withdraw.setState(new WithdrawConfirmedState(withdraw), WithdrawStateEnum.CONFIRMED);
        WithdrawConfirmedState confirmedState = new WithdrawConfirmedState(withdraw);

        confirmedState.modify();

        assertEquals(WithdrawStateEnum.IN_ACCOUNT, withdraw.getStateEnum());
        assertInstanceOf(WithdrawInAccountState.class, withdraw.getState());
    }

    @Test
    @DisplayName("WithdrawInAccountState should do nothing when modified")
    void testWithdrawInAccountStateDoesNothing() {
        withdraw.setState(new WithdrawInAccountState(withdraw), WithdrawStateEnum.IN_ACCOUNT);
        WithdrawInAccountState inAccountState = new WithdrawInAccountState(withdraw);

        WithdrawStateEnum beforeState = withdraw.getStateEnum();

        inAccountState.modify();

        assertEquals(beforeState, withdraw.getStateEnum());

        assertInstanceOf(WithdrawInAccountState.class, withdraw.getState());
    }
}
