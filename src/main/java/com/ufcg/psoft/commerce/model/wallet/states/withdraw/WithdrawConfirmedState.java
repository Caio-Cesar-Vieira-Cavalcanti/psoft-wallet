package com.ufcg.psoft.commerce.model.wallet.states.withdraw;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WithdrawConfirmedState implements WithdrawState {

    @Column(nullable = false)
    WithdrawModel withdraw;

    @Override
    public void modify() {
        this.withdraw.setState(new WithdrawInAccountState(this.withdraw), WithdrawStateEnum.IN_ACCOUNT);
    }
}
