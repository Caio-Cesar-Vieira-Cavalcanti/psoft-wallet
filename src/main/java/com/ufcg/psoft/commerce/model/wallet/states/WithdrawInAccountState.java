package com.ufcg.psoft.commerce.model.wallet.states;

import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class WithdrawInAccountState implements WithdrawState {

    WithdrawModel withdraw;

    @Override
    public void modify() {
        //Do nothing
    }
}
