package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {

    @JsonProperty("wallet_id")
    @NotNull(message = "The 'id' field cannot be null")
    private UUID id;

    @JsonProperty("purchases")
    private List<PurchaseResponseDTO> purchases;
}
