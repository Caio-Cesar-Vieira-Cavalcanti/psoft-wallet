package com.ufcg.psoft.commerce.model.wallet.states.purchase;

import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInWalletState implements PurchaseState {

    PurchaseModel purchase;

    @Override
    public void modify(UserModel user) {
        //Do nothing
    }
}
