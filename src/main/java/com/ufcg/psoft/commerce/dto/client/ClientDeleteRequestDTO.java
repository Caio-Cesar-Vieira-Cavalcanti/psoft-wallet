package com.ufcg.psoft.commerce.dto.client;

import jakarta.validation.constraints.NotBlank;

public class ClientDeleteRequestDTO {
    @NotBlank
    private String accessCode;

    public String getAccessCode() {
        return accessCode;
    }
}
