package com.ufcg.psoft.commerce.model.wallet.states.withdraw;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class WithdrawConfirmedState implements WithdrawState {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawConfirmedState.class);

    @Column(nullable = false)
    WithdrawModel withdraw;

    @Override
    public void modify(UserModel user) {
        // Transição automática para IN_ACCOUNT quando o resgate é processado
        this.withdraw.setState(new WithdrawInAccountState(this.withdraw), WithdrawStateEnum.IN_ACCOUNT);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Withdraw automatically transitioned to IN_ACCOUNT for asset: {}", 
                       withdraw.getAsset().getName());
        }
    }
}
