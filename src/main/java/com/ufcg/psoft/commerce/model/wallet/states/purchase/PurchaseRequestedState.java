package com.ufcg.psoft.commerce.model.wallet.states.purchase;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetQuantityAvailableIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PurchaseRequestedState implements PurchaseState {

    @Column(nullable = false)
    PurchaseModel purchase;

    @Override
    public void modify(UserModel user) {
        if (!user.isAdmin()) {
            throw new UnauthorizedUserAccessException("Only administrators can confirm the availability of this purchase.");
        }

        AssetModel asset = this.purchase.getAsset();

        if (!asset.isActive()) {
            throw new AssetIsInactiveException();
        }

        if (asset.getQuotaQuantity() < purchase.getQuantity()) {
            throw new AssetQuantityAvailableIsInsufficientException(asset.getName(), purchase.getQuantity(), asset.getQuotaQuantity());
        }

        this.purchase.setState(new PurchaseAvailableState(this.purchase), PurchaseStateEnum.AVAILABLE);

//        // 4. Notifica cliente
//        String clientName = this.purchase.getWallet().getClient().getName();
//        System.out.println("📢 Notificação para " + clientName +
//                ": Sua compra do ativo " + asset.getName() +
//                " está disponível (ativo e liquidez confirmados).");
    }
}
