package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseResponseDTO {

    @JsonProperty("id")
    @NotNull(message = "The 'id' field cannot be null")
    private UUID id;

    @JsonProperty("walletId")
    @NotNull(message = "The 'walletId' field cannot be null")
    private UUID walletId;

    @JsonProperty("assetId")
    @NotNull(message = "The 'asset_id' field cannot be null")
    private UUID assetId;

    @JsonProperty("quantity")
    @NotNull(message = "The 'quantity' field cannot be null")
    private Double quantity;

    @JsonProperty("acquisitionPrice")
    @NotNull(message = "The 'acquisitionPrice' field cannot be null")
    private double acquisitionPrice;

    @JsonProperty("state")
    @NotNull(message = "The 'state' field cannot be null")
    private PurchaseStateEnum purchaseState;

    @JsonProperty("date")
    @NotNull(message = "The 'date' field cannot be null")
    private LocalDate date;

    public PurchaseResponseDTO(PurchaseModel purchase) {
        this.id = purchase.getId();
        this.walletId = purchase.getWallet().getId();
        this.assetId = purchase.getAsset().getId();
        this.quantity = purchase.getQuantity();
        this.purchaseState = purchase.getStateEnum();
        this.date = purchase.getDate();
    }

    public PurchaseResponseDTO(PurchaseResponseAfterAddedInWalletDTO dto) {
        this.id = dto.getId();
        this.walletId = dto.getWalletId();
        this.assetId = dto.getAssetId();
        this.quantity = dto.getQuantity();
        this.purchaseState = dto.getPurchaseState();
        this.date = dto.getDate();
    }
}
