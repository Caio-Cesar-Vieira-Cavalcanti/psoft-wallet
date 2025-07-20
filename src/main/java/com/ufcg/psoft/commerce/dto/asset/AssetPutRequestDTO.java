package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetPutRequestDTO {

    @JsonProperty("status")
    @NotBlank(message = "Required status")
    private boolean status;

    @JsonProperty("quotation")
    @NotBlank(message = "Required quotation")
    private double quotation;

}
