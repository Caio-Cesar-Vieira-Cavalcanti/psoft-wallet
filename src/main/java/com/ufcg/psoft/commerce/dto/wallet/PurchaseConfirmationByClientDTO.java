package com.ufcg.psoft.commerce.dto.wallet;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseConfirmationByClientDTO {

    @NotBlank
    private String accessCode;

}
