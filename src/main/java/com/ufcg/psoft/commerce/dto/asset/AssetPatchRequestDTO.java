package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetPatchRequestDTO {

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("quotation")
    @PositiveOrZero(message = "Quotation must be zero or a positive number")
    private Double quotation;

    @JsonProperty("quota_quantity")
    @PositiveOrZero(message = "Asset quotation quantity must be positive or zero!")
    private Double quotaQuantity;
}
