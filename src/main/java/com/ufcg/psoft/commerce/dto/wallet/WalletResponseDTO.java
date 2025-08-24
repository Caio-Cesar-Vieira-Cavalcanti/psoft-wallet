package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {

    @JsonProperty("walletId")
    private UUID id;

    @JsonProperty("budget")
    private double budget;
}
