package com.ufcg.psoft.commerce.dto.client;

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
public class ClientPurchaseHistoryRequestDTO {

    @JsonProperty("accessCode")
    @NotNull(message = "The 'access_code' field cannot be null")
    private String accessCode;
}
