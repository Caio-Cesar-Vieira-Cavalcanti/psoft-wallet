package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseStateNotInitializedException;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseModel extends TransactionModel {

    @Column(name = "acquisitionPrice", nullable = false)
    private double acquisitionPrice;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PurchaseStateEnum stateEnum = PurchaseStateEnum.REQUESTED;

    @Transient
    private PurchaseState state;

    @PostLoad
    public void loadState() {
        switch (stateEnum) {
            case REQUESTED -> this.state = new PurchaseRequestedState(this);
            case AVAILABLE -> this.state = new PurchaseAvailableState(this);
            case PURCHASED -> this.state = new PurchasePurchasedState(this);
            case IN_WALLET -> this.state = new PurchaseInWalletState(this);
            default -> {
                this.state = new PurchaseRequestedState(this);
                this.stateEnum = PurchaseStateEnum.REQUESTED;
            }
        }
    }

    public void modify(UserModel user) {
        if (this.state == null) {
            throw new PurchaseStateNotInitializedException();
        }
        this.state.modify(user);
    }

    public void setState(PurchaseState newState, PurchaseStateEnum type) {
        this.state = newState;
        this.stateEnum = type;
    }
}
