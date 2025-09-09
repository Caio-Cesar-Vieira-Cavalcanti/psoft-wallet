package com.ufcg.psoft.commerce.dto.client;

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
public class ClientExportTransactionsResponseDTO {

    @NotNull
    @NotBlank
    private String fileName;

    @NotNull
    private byte[] content;

}
