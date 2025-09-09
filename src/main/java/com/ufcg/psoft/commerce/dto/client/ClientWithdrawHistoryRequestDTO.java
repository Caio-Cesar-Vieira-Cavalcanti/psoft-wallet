package com.ufcg.psoft.commerce.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientWithdrawHistoryRequestDTO {

    @JsonProperty("accessCode")
    @NotNull(message = "The 'accessCode' field cannot be null")
    @NotBlank(message = "The 'accessCode' field cannot be blank")
    private String accessCode;

    @JsonProperty("assetType")
    private AssetTypeEnum assetType;

    @JsonProperty("withdrawPeriod")
    private LocalDate date;

    @JsonProperty("withdrawState")
    private WithdrawStateEnum withdrawState;
}
