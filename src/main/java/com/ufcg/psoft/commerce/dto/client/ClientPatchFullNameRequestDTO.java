package com.ufcg.psoft.commerce.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPatchFullNameRequestDTO {
    @NotBlank
    private String fullName;

    @NotBlank
    private String accessCode;
}
