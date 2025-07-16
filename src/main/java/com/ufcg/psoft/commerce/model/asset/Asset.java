package com.ufcg.psoft.commerce.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

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
    @Column(nullable = false)
    private Boolean status;

    @JsonProperty("quotation")
    @Column(nullable = false)
    private Double quotation;

    @JsonProperty("quota_quantity")
    @Column(nullable = false)
    private Double quotaQuantity;

}
