package com.ufcg.psoft.commerce.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationReportRequestDTO {

    @JsonProperty("adminEmail")
    @NotNull(message = "The 'adminEmail' cannot be null")
    @NotBlank(message = "The 'adminEmail' cannot be blank")
    private String adminEmail;

    @JsonProperty("adminAccessCode")
    @NotNull(message = "The 'adminAccessCode' cannot be null")
    @NotBlank(message = "The 'adminAccessCode' cannot be blank")
    private String adminAccessCode;

    private AssetTypeEnum assetType;
    private UUID clientId;
    private OperationTypeEnum operationType;

    @PastOrPresent(message = "dateFrom cannot be in the future")
    private LocalDate dateFrom;

    @PastOrPresent(message = "dateTo cannot be in the future")
    private LocalDate dateTo;

    @AssertTrue(message = "dateFrom must be on/before dateTo")
    public boolean isDateRangeValid() {
        if (dateFrom == null || dateTo == null) return true;
        return !dateFrom.isAfter(dateTo);
    }
}
