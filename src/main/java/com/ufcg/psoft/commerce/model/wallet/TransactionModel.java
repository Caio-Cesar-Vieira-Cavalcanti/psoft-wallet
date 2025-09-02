package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class TransactionModel {

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

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @PostLoad
    public abstract void loadState();
}
