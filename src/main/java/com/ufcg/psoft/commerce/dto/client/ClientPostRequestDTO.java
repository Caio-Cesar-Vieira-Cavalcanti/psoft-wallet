package com.ufcg.psoft.commerce.dto.client;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientPostRequestDTO {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String accessCode;

    @NotNull
    private PlanTypeEnum planType;

    @NotNull
    private Double budget;

    @NotNull
    private AddressDTO address;
}
