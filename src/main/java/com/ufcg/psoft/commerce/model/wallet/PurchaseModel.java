package com.ufcg.psoft.commerce.model.wallet;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "purchase")
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

    private Double quantity;

    private PurchaseState state;

    private LocalDate date;
}
