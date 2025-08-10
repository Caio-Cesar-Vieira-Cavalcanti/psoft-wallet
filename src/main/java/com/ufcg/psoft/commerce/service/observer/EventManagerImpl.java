package com.ufcg.psoft.commerce.service.observer;

import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.notification.AlreadySubscribedException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIsNotPremium;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
import com.ufcg.psoft.commerce.model.observer.ISubscriber;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.repository.observer.SubscriptionRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventManagerImpl implements EventManager {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ClientRepository clientRepository;

    public SubscriptionResponseDTO subscribeToAssetEvent(UUID assetId, UUID subscriberId, SubscriptionTypeEnum subscriptionType) {
        this.validateClient(subscriberId, subscriptionType);
        this.ensureNotAlreadySubscribed(assetId, subscriberId, subscriptionType);

        SubscriptionModel subscription = new SubscriptionModel();
        subscription.setAssetId(assetId);
        subscription.setSubscriberId(subscriberId);
        subscription.setSubscriptionType(subscriptionType);

        subscriptionRepository.save(subscription);

        return SubscriptionResponseDTO.builder()
                .message("Subscription registered successfully")
                .assetId(assetId)
                .clientId(subscriberId)
                .subscriptionType(subscriptionType)
                .build();
    }

    public void notifySubscribersByType(UUID assetId, SubscriptionTypeEnum subscriptionType) {
        List<SubscriptionModel> subscriptions = getSubscriptionsByType(assetId, subscriptionType);

        String contextMessage = String.format(
                "You are receiving a '%s' type notification regarding the asset with ID: %s",
                formatSubscriptionType(subscriptionType),
                assetId
        );

        subscriptions.forEach(subscription -> {
            UUID clientId = subscription.getSubscriberId();

            ISubscriber subscriber = getValidSubscriber(clientId);
            subscriber.notify(contextMessage);
            this.subscriptionRepository.deleteById(subscription.getId());
        });
    }

    private String formatSubscriptionType(SubscriptionTypeEnum type) {
        return switch (type) {
            case AVAILABILITY -> "availability";
            case PRICE_VARIATION -> "price variation";
        };
    }

    private List<SubscriptionModel> getSubscriptionsByType(UUID assetId, SubscriptionTypeEnum subscriptionType) {
        return subscriptionRepository.findByAssetIdAndSubscriptionType(assetId, subscriptionType);
    }

    private ISubscriber getValidSubscriber(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(client -> (ISubscriber) client)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));
    }

    private void validateClientExists(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientIdNotFoundException(clientId);
        }
    }

    private void validateClient(UUID subscriberId, SubscriptionTypeEnum subscriptionType) {
        this.validateClientExists(subscriberId);

        ClientModel clientModel = this.clientRepository.findById(subscriberId)
                .orElseThrow(() -> new ClientIdNotFoundException(subscriberId));

        if (subscriptionType == SubscriptionTypeEnum.PRICE_VARIATION) {
            if (clientModel.getPlanType() != PlanTypeEnum.PREMIUM) throw new ClientIsNotPremium();
        }
    }

    private void ensureNotAlreadySubscribed(UUID assetId, UUID clientId, SubscriptionTypeEnum subscriptionType) {
        boolean alreadySubscribed = subscriptionRepository
                .findByAssetIdAndSubscriptionType(assetId, subscriptionType)
                .stream()
                .anyMatch(sub -> sub.getSubscriberId().equals(clientId));

        if (alreadySubscribed) {
            throw new AlreadySubscribedException(assetId, subscriptionType);
        }
    }
}
