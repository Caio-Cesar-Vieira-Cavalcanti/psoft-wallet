package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetActivationPatchRequestDTO {

    @JsonProperty("isActive")
    @NotNull(message = "The 'is_active' field cannot be null")
    private Boolean isActive;

    @JsonProperty("adminEmail")
    @NotNull(message = "The 'admin_email' field cannot be null")
    private String adminEmail;

    @JsonProperty("adminAccessCode")
    @NotNull(message = "The 'admin_access_code_email' cannot be null")
    private String adminAccessCode;
}
