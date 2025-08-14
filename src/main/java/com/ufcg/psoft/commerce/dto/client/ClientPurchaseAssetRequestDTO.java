package com.ufcg.psoft.commerce.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPurchaseAssetRequestDTO {

    @JsonProperty("accessCode")
    @NotNull(message = "The 'access_code' field cannot be null")
    private String accessCode;

    @JsonProperty("assetQuantity")
    @NotNull(message = "The 'assetQuantity' field cannot be null")
    private Integer assetQuantity;
}