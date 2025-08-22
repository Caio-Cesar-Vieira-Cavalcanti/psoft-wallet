package com.ufcg.psoft.commerce.model.wallet.states.purchase;

import com.ufcg.psoft.commerce.model.user.UserModel;

public interface PurchaseState {
    void modify(UserModel user);
}
