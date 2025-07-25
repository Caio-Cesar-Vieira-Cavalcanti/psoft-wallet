package com.ufcg.psoft.commerce.dto.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetDeleteRequestDTO {
    @JsonProperty("admin_email")
    @NotNull(message = "The 'admin_email' field cannot be null")
    private String adminEmail;

    @JsonProperty("admin_access_code")
    @NotNull(message = "The 'admin_access_code_email' cannot be null")
    private String adminAccessCode;
}
