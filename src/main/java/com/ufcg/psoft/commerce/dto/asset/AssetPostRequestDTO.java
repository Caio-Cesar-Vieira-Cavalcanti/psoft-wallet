package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
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
    private String name;

    // revisar sobre o atributo do tipo do asset
    // ideia: no controller, criar um endpoint para retornar o id de todos os tipos de assets criados no sistema
    @JsonProperty("type")
    @NotBlank(message = "Required asset type")
    @PositiveOrZero(message = "Asset type id must be positive or zero!")
    private Long type;

    @JsonProperty("description")
    @NotBlank(message = "Required asset description")
    private String description;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("quotation")
    @PositiveOrZero(message = "Asset quotation must be positive or zero!")
    private double quotation;

    @JsonProperty("quota_quantity")
    @PositiveOrZero(message = "Asset quotation quantity must be positive or zero!")
    private int quotaQuantity;
}
