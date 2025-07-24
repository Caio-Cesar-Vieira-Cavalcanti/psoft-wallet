package com.ufcg.psoft.commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ClientDeleteRequestDTO {
    @NotBlank
    private String accessCode;

    public String getAccessCode() {
        return accessCode;
    }
}
