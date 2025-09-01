package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawHistoryResponseDTO {

    @JsonProperty("withdrawId")
    private UUID withdrawId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetId")
    private UUID assetId;

    @JsonProperty("quantityWithdrawn")
    private double quantityWithdrawn;

    @JsonProperty("sellingPrice")
    private double sellingPrice;

    @JsonProperty("totalValue")
    private double totalValue;

    @JsonProperty("tax")
    private double tax;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("state")
    private WithdrawStateEnum state;
}
