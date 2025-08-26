package com.ufcg.psoft.commerce.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientWithdrawAssetRequestDTO {

    @JsonProperty("accessCode")
    @NotNull(message = "The 'accessCode' field cannot be null")
    @NotBlank(message = "The 'accessCode' field cannot be blank")
    private String accessCode;

    @JsonProperty("quantityToWithdraw")
    @Min(1)
    private double quantityToWithdraw;
}
