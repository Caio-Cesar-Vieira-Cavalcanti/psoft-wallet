package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawConfirmedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawInAccountState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawRequestedState;
import com.ufcg.psoft.commerce.model.wallet.states.withdraw.WithdrawState;
import com.ufcg.psoft.commerce.model.user.UserModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Function;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WithdrawModel extends TransactionModel {

    private static final Map<WithdrawStateEnum, Function<WithdrawModel, WithdrawState>> STATE_FACTORIES =
            Map.of(
                    WithdrawStateEnum.REQUESTED, WithdrawRequestedState::new,
                    WithdrawStateEnum.CONFIRMED, WithdrawConfirmedState::new,
                    WithdrawStateEnum.IN_ACCOUNT, WithdrawInAccountState::new
            );

    @Column(name = "sellingPrice", nullable = false)
    private double sellingPrice;

    @Column(name = "tax", nullable = false)
    private double tax;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private WithdrawStateEnum stateEnum;

    @Transient
    private WithdrawState state;

    @PostLoad
    public void loadState() {
        this.state = STATE_FACTORIES
                .getOrDefault(stateEnum, WithdrawRequestedState::new)
                .apply(this);
    }

    public void modify(UserModel user) {
        if (this.state == null) {
            this.loadState();
        }
        this.state.modify(user);
    }

    public void setState(WithdrawState newState, WithdrawStateEnum type) {
        this.state = newState;
        this.stateEnum = type;
    }
}
