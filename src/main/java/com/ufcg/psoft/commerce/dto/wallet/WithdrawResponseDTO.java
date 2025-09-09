package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawResponseDTO {

    @JsonProperty("withdrawId")
    private UUID withdrawId;

    @JsonProperty("walletId")
    private UUID walletId;

    @JsonProperty("assetId")
    private UUID assetId;

    @JsonProperty("quantityWithdrawn")
    private double quantityWithdrawn;

    @JsonProperty("valueReceived")
    private double valueReceived;

    @JsonProperty("newWalletBudget")
    private double newWalletBudget;
    
    @JsonProperty("state")
    private WithdrawStateEnum state;
}
