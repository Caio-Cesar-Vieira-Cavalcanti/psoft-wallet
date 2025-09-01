package com.ufcg.psoft.commerce.model.wallet.states.withdraw;

import com.ufcg.psoft.commerce.model.user.UserModel;

@FunctionalInterface
public interface WithdrawState {
    void modify(UserModel user);
}
