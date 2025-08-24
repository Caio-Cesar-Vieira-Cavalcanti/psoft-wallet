package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletHoldingResponseDTO {

    @JsonProperty("wallet")
    private WalletResponseDTO walletResponseDTO;

    @JsonProperty("holdings")
    private List<HoldingResponseDTO> holdings;

    @JsonProperty("totalInvested")
    private double totalInvested;

    @JsonProperty("totalCurrent")
    private double totalCurrent;

    @JsonProperty("totalPerformance")
    private double totalPerformance;
}
