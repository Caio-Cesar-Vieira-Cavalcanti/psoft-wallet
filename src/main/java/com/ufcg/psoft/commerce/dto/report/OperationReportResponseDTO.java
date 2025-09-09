package com.ufcg.psoft.commerce.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationReportResponseDTO {
    @JsonProperty("operationId")
    private UUID operationId;

    @JsonProperty("operationType")
    private OperationTypeEnum operationType;

    @JsonProperty("clientId")
    private UUID clientId;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("assetId")
    private UUID assetId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetTypeEnum assetType;

    @JsonProperty("quantity")
    private double quantity;

    @JsonProperty("grossValue")
    private double gross;

    @JsonProperty("tax")
    private Double tax;

    @JsonProperty("netValue")
    private Double net;

    @JsonProperty("occurredAt")
    private LocalDateTime occurredAt;
}
