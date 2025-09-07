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
    @JsonProperty("operation_id")
    private UUID operationId;

    @JsonProperty("operation_type")
    private OperationTypeEnum operationType;

    @JsonProperty("client_id")
    private UUID clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("asset_id")
    private UUID assetId;

    @JsonProperty("asset_name")
    private String assetName;

    @JsonProperty("asset_type")
    private AssetTypeEnum assetType;

    @JsonProperty("quantity")
    private double quantity;

    @JsonProperty("gross_value")
    private double gross;

    @JsonProperty("tax")
    private Double tax;

    @JsonProperty("net_value")
    private Double net;

    @JsonProperty("occurred_at")
    private LocalDateTime occurredAt;
}
