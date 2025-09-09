package com.ufcg.psoft.commerce.model.wallet.states.withdraw;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class WithdrawRequestedState implements WithdrawState {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawRequestedState.class);

    @Column(nullable = false)
    WithdrawModel withdraw;

    @Override
    public void modify(UserModel user) {
        if (!user.isAdmin()) {
            throw new UnauthorizedUserAccessException("Only administrators can confirm this withdrawal request.");
        }

        AssetModel asset = this.withdraw.getAsset();
        HoldingModel holding = findHolding(this.withdraw.getWallet(), asset);

        // Validar se o cliente possui quantidade suficiente do ativo
        holding.validateQuantityToWithdraw(withdraw.getQuantity());

        this.withdraw.setState(new WithdrawConfirmedState(this.withdraw), WithdrawStateEnum.CONFIRMED);

        notifyClientAboutWithdrawConfirmation(asset);
    }

    private HoldingModel findHolding(com.ufcg.psoft.commerce.model.wallet.WalletModel wallet, AssetModel asset) {
        return wallet.getHoldings()
                .values()
                .stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElseThrow(() -> new ClientHoldingIsInsufficientException(
                        "Client does not own asset " + asset.getName()
                ));
    }

    private void notifyClientAboutWithdrawConfirmation(AssetModel asset) {
        try {
            final String cyan = "\u001B[36m";
            final String reset = "\u001B[0m";
            final String bold = "\u001B[1m";

            String notificationMessage = String.format("""
                %s%sWITHDRAW REQUEST CONFIRMED%s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Asset: %s
                Status: Withdraw request confirmed by administrator
                Reason: Withdrawal request approved and ready for processing
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%s""",
                cyan, bold, reset,
                asset.getName(),
                reset
            );

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Withdraw request confirmed for asset: {}", asset.getName());
            }
            LOGGER.info("Withdraw Notification:\n{}", notificationMessage);

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error generating withdraw confirmation notification: {}", e.getMessage());
            }

            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Fallback notification: Withdraw request confirmed for asset {}", asset.getName());
            }
        }
    }
}
