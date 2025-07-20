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
    @PositiveOrZero(message = "Asset type id is positive or zero!")
    private Long type;

    @JsonProperty("description")
    @NotBlank(message = "Required asset description")
    private String description;

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("quotation")
    @PositiveOrZero(message = "Asset quotation is positive or zero!")
    private double quotation;

    @JsonProperty("quota_quantity")
    @PositiveOrZero(message = "Asset quotation quantity is positive or zero!")
    private double quotaQuantity;
}
