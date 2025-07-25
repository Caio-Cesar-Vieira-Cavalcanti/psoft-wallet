package com.ufcg.psoft.commerce.dto.client;

import jakarta.validation.constraints.NotBlank;

public class AddressDTO {
    @NotBlank
    private String street;

    @NotBlank
    private String number;

    @NotBlank
    private String neighborhood;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String zipCode;
}
