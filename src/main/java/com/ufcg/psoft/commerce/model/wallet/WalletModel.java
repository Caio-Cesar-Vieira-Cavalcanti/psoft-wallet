package com.ufcg.psoft.commerce.model.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private double budget;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<UUID, HoldingModel> holdings;

    public void decreaseBudgetAfterPurchase(double purchaseValue) {
        this.budget -= purchaseValue;
    }

    public void increaseBudgetAfterWithdraw(double withdrawValue) {
        this.budget += withdrawValue;
    }
}
