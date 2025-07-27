package com.ufcg.psoft.commerce.dto.asset;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetQuotationUpdateDTO {

    @NotNull(message = "Quotation cannot be null")
    @PositiveOrZero(message = "Quotation must be a positive value or zero")
    private Double quotation;

    @NotNull(message = "Admin email is required")
    private String adminEmail;

    @NotNull(message = "Admin access code is required")
    private String adminAccessCode;
}