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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class PurchaseRequestedState implements PurchaseState {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseRequestedState.class);
    
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

        notifyClientAboutPurchaseAvailability(asset);
    }

    private void notifyClientAboutPurchaseAvailability(AssetModel asset) {
        try {
            final String MAGENTA = "\u001B[35m";
            final String RESET = "\u001B[0m";
            final String BOLD = "\u001B[1m";

            String assetName = asset.getName();
            double quantity = purchase.getQuantity();
            double assetPrice = asset.getQuotation();
            double totalValue = quantity * assetPrice;

            String notificationMessage = String.format("""
                %s%sPURCHASE AVAILABILITY NOTIFICATION%s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Asset: %s
                Quantity: %.2f
                Unit Price: $ %.2f
                Total Value: $ %.2f
                Status: Asset and liquidity confirmed
                Reason: Purchase available for execution
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%s""",
                MAGENTA, BOLD, RESET,
                assetName, quantity, assetPrice, totalValue,
                RESET
            );

            if (logger.isInfoEnabled()) {
                logger.info("Availability notification sent - Asset: {}, Quantity: {}, Value: $ {}", 
                           assetName, quantity, String.format("%.2f", totalValue));
            }
            logger.info("Purchase Notification:\n{}", notificationMessage);
            
        } catch (Exception e) {
            logger.error("Error generating availability notification: {}", e.getMessage());
            logger.warn("Fallback notification: Purchase available for asset {}", asset.getName());
        }
    }
}
