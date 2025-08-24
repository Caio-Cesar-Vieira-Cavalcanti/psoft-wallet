package com.ufcg.psoft.commerce.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PurchaseConfirmationRequestDTO {

    @JsonProperty("adminEmail")
    @NotNull(message = "The 'adminEmail' cannot be null")
    @NotBlank(message = "The 'adminEmail' cannot be blank")
    private String adminEmail;

    @JsonProperty("adminAccessCode")
    @NotNull(message = "The 'adminAccessCode' cannot be null")
    @NotBlank(message = "The 'adminAccessCode' cannot be blank")
    private String adminAccessCode;
}
