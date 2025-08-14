package com.ufcg.psoft.commerce.model.wallet.states;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseState;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PurchaseAvailableState implements PurchaseState {

    @Column(nullable = false)
    PurchaseModel purchase;

    @Override
    public void modify() {
        this.purchase.setState(new PurchasePurchasedState(this.purchase), PurchaseStateEnum.PURCHASED);
    }
}
