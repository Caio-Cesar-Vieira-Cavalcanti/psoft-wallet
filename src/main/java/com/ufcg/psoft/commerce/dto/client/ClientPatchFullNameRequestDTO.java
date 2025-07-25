package com.ufcg.psoft.commerce.dto.client;

import jakarta.validation.constraints.NotBlank;

public class ClientPatchFullNameRequestDTO {
    @NotBlank
    private String fullName;

    @NotBlank
    private String accessCode;

    public String getFullName() {
        return fullName;
    }

    public String getAccessCode() {
        return accessCode;
    }
}
