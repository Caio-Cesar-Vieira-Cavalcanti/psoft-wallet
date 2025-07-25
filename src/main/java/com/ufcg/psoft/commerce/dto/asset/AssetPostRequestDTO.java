package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.ufcg.psoft.commerce.model.asset.AssetTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetPostRequestDTO {
    @JsonProperty("name")
    @NotBlank(message = "Required asset name")
    @NotNull
    private String name;

    @JsonProperty("assetType")
    @NotNull(message = "Required asset type")
    private AssetTypeEnum assetType;

    @JsonProperty("description")
    @NotBlank(message = "Required asset description")
    @NotNull
    private String description;

    @JsonProperty("is_active")
    @NotNull
    private Boolean isActive;

    @JsonProperty("quotation")
    @PositiveOrZero(message = "Asset quotation must be positive or zero!")
    @NotNull
    private Double quotation;

    @JsonProperty("quota_quantity")
    @PositiveOrZero(message = "Asset quotation quantity must be positive or zero!")
    @NotNull
    private Double quotaQuantity;

    @JsonProperty("admin_email")
    @NotNull(message = "The 'admin_email' field cannot be null")
    private String adminEmail;

    @JsonProperty("admin_access_code")
    @NotNull(message = "The 'admin_access_code_email' cannot be null")
    private String adminAccessCode;
}
