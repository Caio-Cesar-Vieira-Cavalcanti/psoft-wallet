package com.ufcg.psoft.commerce.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "TB_ASSET" )
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

    @JsonProperty("type")
    @OneToOne
    private AssetType type;

    @JsonProperty("description")
    @Column(nullable = false)
    private String description;

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("quotation")
    private double quotation;

    @JsonProperty("quota_quantity")
    private double quotaQuantity;

}
