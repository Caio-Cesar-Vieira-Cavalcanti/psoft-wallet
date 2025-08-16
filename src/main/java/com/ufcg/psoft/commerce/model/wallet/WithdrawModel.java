package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.wallet.states.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WithdrawModel extends TransactionModel {

    @Column(name = "sellingPrice", nullable = false)
    private double sellingPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private WithdrawStateEnum stateEnum;

    @Transient
    private WithdrawState state;

    @PostLoad
    public void loadState() {
        switch (stateEnum) {
            case REQUESTED -> this.state = new WithdrawRequestedState(this);
            case CONFIRMED -> this.state = new WithdrawConfirmedState(this);
            case IN_ACCOUNT -> this.state = new WithdrawInAccountState(this);
            default -> {
                this.state = new WithdrawRequestedState(this);
                this.stateEnum = WithdrawStateEnum.REQUESTED;
            }
        }
    }

    public void setState(WithdrawState newState, WithdrawStateEnum type) {
        this.state = newState;
        this.stateEnum = type;
    }
}
