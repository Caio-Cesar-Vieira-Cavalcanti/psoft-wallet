package com.ufcg.psoft.commerce.model.wallet.states.purchase;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PurchasePurchasedState implements PurchaseState {

    @Column(nullable = false)
    PurchaseModel purchase;

    @Override
    public void modify(UserModel user) {
        this.purchase.setState(new PurchaseInWalletState(this.purchase), PurchaseStateEnum.IN_WALLET);
    }
}
