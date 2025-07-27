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
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "assetId", nullable = false)
    private AssetModel asset;

    @ManyToOne
    @JoinColumn(name = "walletId", nullable = false)
    private WalletModel wallet;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private LocalDate date;
}
