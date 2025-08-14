package com.ufcg.psoft.commerce.model.wallet.states;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawState;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WithdrawRequestedState implements WithdrawState {

    @Column(nullable = false)
    WithdrawModel withdraw;

    @Override
    public void modify() {
        this.withdraw.setState(new WithdrawConfirmedState(this.withdraw), WithdrawStateEnum.CONFIRMED);
    }
}
