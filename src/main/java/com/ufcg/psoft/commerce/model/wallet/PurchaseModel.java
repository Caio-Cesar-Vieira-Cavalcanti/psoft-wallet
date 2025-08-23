package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseStateNotInitializedException;
import com.ufcg.psoft.commerce.model.user.UserModel;
import com.ufcg.psoft.commerce.model.wallet.states.purchase.*;
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
public class PurchaseModel extends TransactionModel {

    private static final Map<PurchaseStateEnum, Function<PurchaseModel, PurchaseState>> STATE_FACTORIES =
            Map.of(
                    PurchaseStateEnum.REQUESTED, PurchaseRequestedState::new,
                    PurchaseStateEnum.AVAILABLE, PurchaseAvailableState::new,
                    PurchaseStateEnum.PURCHASED, PurchasePurchasedState::new,
                    PurchaseStateEnum.IN_WALLET, PurchaseInWalletState::new
            );

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
        this.state = STATE_FACTORIES
                .getOrDefault(stateEnum, PurchaseRequestedState::new)
                .apply(this);
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
