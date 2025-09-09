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
    @NotBlank
    private UUID assetId;

    @NotNull
    @NotBlank
    private double quantity;

    @NotNull
    @NotBlank
    private double totalValue;

    private double tax;

    @NotNull
    @NotBlank
    private LocalDate date;

    @NotNull
    @NotBlank
    private String state;
}
