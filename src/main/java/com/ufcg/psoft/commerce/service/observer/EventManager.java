package com.ufcg.psoft.commerce.service.observer;

import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientMarkInterestInAssetRequestDTO;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;

import java.util.UUID;

public interface EventManager {

    SubscriptionResponseDTO subscribeToAssetEvent(UUID assetId, UUID idSubscriber, SubscriptionTypeEnum subscriptionType);

    void notifySubscribersByType(UUID assetId, SubscriptionTypeEnum subscriptionType);
}
