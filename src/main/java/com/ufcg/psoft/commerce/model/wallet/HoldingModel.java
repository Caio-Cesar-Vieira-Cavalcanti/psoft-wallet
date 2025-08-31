package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HoldingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "assetId", nullable = false)
    private AssetModel asset;

    @ManyToOne
    @JoinColumn(name = "walletId", nullable = false)
    private WalletModel wallet;

    @Column(name = "quantity", nullable = false)
    @Setter
    private double quantity;

    @Column(name = "accumulatedPrice", nullable = false)
    @Setter
    private double accumulatedPrice;

    public void increaseQuantityAfterPurchase(double purchaseQuantity) {
        this.quantity += purchaseQuantity;
    }

    public void increaseAccumulatedPrice(double purchaseAccumulatedPrice) {
        this.accumulatedPrice += purchaseAccumulatedPrice;
    }

    public void validateQuantityToWithdraw(double quantityToWithdraw) {
        if (this.quantity < quantityToWithdraw) {
            throw new ClientHoldingIsInsufficientException(
                    "Holding quantity " + this.quantity +
                            " is less than requested withdrawal " + quantityToWithdraw
            );
        }
    }

    public void decreaseQuantityAfterWithdraw(double quantityToWithdraw) {
        this.quantity -= quantityToWithdraw;
    }

    public void decreaseAccumulatedPriceAfterWithdraw(double quantityToWithdraw, double assetQuotation) {
        this.accumulatedPrice -= quantityToWithdraw * assetQuotation;
    }

}
