package com.ufcg.psoft.commerce.dto.wallet;

import jakarta.validation.constraints.NotBlank;
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
public class TransactionExportDTO {

    @NotNull
    @NotBlank
    private String type;

    @NotNull
    private UUID assetId;

    private double quantity;

    private double totalValue;

    private double tax;

    @NotNull
    private LocalDate date;

    @NotNull
    @NotBlank
    private String state;
}
