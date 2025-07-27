package com.ufcg.psoft.commerce.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "asset")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetModel {

    @JsonProperty("id")
    @Id
    @GeneratedValue
    private UUID id;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("assetType")
    @ManyToOne
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;

    @JsonProperty("description")
    @Column(nullable = false)
    private String description;

    @JsonProperty("isActive")
    @Column(nullable = false)
    private boolean isActive;

    @JsonProperty("quotation")
    @Column(nullable = false)
    private double quotation;

    @JsonProperty("quota_quantity")
    @Column(nullable = false)
    private double quotaQuantity;

}
