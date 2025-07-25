package com.ufcg.psoft.commerce.dto.client;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class ClientResponseDTO {
    @NotNull
    private UUID id;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private AddressModel address;

    @NotNull
    private PlanTypeEnum planType;

    @NotNull
    private double budget;
    //private WalletModel wallet;

}
