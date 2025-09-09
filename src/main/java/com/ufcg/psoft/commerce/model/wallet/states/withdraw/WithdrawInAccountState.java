package com.ufcg.psoft.commerce.model.wallet.states.withdraw;

import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class WithdrawInAccountState implements WithdrawState {

    WithdrawModel withdraw;

    @Override
    public void modify(UserModel user) {
        //Do nothing
    }
}
