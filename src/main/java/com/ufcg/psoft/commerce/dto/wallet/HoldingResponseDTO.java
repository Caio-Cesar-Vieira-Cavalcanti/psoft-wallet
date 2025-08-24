package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingResponseDTO {

    @JsonProperty("assetId")
    private UUID assetId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetTypeResponseDTO assetType;

    @JsonProperty("quantity")
    private double quantity;

    @JsonProperty("acquisitionPrice")
    private double acquisitionPrice;

    @JsonProperty("currentPrice")
    private double currentPrice;

    @JsonProperty("performance")
    private double performance;

    @JsonIgnore
    private double acquisitionTotal;

    @JsonIgnore
    private double currentTotal;

}
