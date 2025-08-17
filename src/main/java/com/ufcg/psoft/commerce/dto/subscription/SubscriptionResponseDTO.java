package com.ufcg.psoft.commerce.dto.subscription;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDTO {

    @JsonProperty("message")
    private String message;

    @JsonProperty("asset_id")
    private UUID assetId;

    @JsonProperty("client_id")
    private UUID clientId;

    @JsonProperty("subscription_type")
    private SubscriptionTypeEnum subscriptionType;
}
