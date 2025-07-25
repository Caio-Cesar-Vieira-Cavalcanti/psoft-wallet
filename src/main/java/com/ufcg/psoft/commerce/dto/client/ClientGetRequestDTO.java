package com.ufcg.psoft.commerce.dto.client;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClientGetRequestDTO {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private PlanTypeEnum planType;

    @NotNull
    private Double budget;

    @NotNull
    private AddressDTO address;
}
