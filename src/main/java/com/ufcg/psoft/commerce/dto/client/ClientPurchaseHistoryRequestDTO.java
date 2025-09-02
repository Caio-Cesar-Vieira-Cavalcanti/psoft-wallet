package com.ufcg.psoft.commerce.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPurchaseHistoryRequestDTO {

    @JsonProperty("accessCode")
    @NotNull(message = "The 'accessCode' field cannot be null")
    @NotBlank(message = "The 'accessCode' field cannot be blank")
    private String accessCode;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("purchasePeriod")
    private LocalDate date;

    @JsonProperty("purchaseState")
    private PurchaseStateEnum purchaseState;
}
