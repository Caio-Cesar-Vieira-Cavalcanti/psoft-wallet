package com.ufcg.psoft.commerce.model.wallet.states;

import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInWalletState implements PurchaseState {

    PurchaseModel purchase;

    @Override
    public void modify() {
        //Do nothing
    }
}
