package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HoldingModel {

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
    private double quantity;

    @Column(name = "accumulatedPrice", nullable = false)
    private double accumulatedPrice;
}
