package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.enums.PurchaseState;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "purchase_model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private AssetModel asset;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private WalletModel wallet;

    private Double quantity;

    @Enumerated(EnumType.STRING)
    private PurchaseState state;

    private LocalDate date;
}
