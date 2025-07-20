package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetResponseDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private AssetType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("quotation")
    private double quotation;

    @JsonProperty("quota_quantity")
    private double quotaQuantity;

    public AssetResponseDTO(AssetModel assetModel) {
        this.id = assetModel.getId();
        this.name = assetModel.getName();
        this.type = assetModel.getType();
        this.description = assetModel.getDescription();
        this.status = assetModel.isStatus();
        this.quotation = assetModel.getQuotation();
        this.quotaQuantity = assetModel.getQuotaQuantity();
    }
}
