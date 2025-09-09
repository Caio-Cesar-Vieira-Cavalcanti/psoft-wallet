package com.ufcg.psoft.commerce.dto.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportTransactionsResponseDTO {

    @NotNull
    @NotBlank
    private String fileName;

    @NotNull
    private byte[] content;

}
