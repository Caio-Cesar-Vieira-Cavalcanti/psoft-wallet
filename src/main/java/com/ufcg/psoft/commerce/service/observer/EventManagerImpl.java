package com.ufcg.psoft.commerce.service.observer;

import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientMarkInterestInAssetRequestDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsAlreadyActive;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactive;
import com.ufcg.psoft.commerce.exception.asset.AssetIsNotStockNeitherCrypto;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIsNotPremium;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
import com.ufcg.psoft.commerce.model.observer.ISubscriber;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.repository.observer.SubscriptionRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventManagerImpl implements EventManager {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public SubscriptionResponseDTO subscribeToAssetEvent(ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO, UUID idSubscriber, SubscriptionTypeEnum subscriptionType) {
        UUID idAsset = clientMarkInterestInAssetRequestDTO.getAssetId();

        this.validateClient(idSubscriber, clientMarkInterestInAssetRequestDTO, subscriptionType);
        this.validateAsset(idAsset, subscriptionType);

        boolean alreadySubscribed = subscriptionRepository
                .findByAssetIdAndSubscriptionType(idAsset, subscriptionType)
                .stream()
                .anyMatch(sub -> sub.getClientId().equals(idSubscriber));

        if (!alreadySubscribed) {
            SubscriptionModel subscription = new SubscriptionModel();
            subscription.setAssetId(idAsset);
            subscription.setClientId(idSubscriber);
            subscription.setQuotationAtMoment(this.getQuotationAtMoment(idAsset));
            subscription.setSubscriptionType(subscriptionType);

            subscriptionRepository.save(subscription);
        }

        return SubscriptionResponseDTO.builder()
                .message("Subscription registered successfully")
                .assetId(idAsset)
                .clientId(idSubscriber)
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
            UUID clientId = subscription.getClientId();

            if (subscriptionType == SubscriptionTypeEnum.PRICE_VARIATION) {
                if (!priceVariationIsValidToNotify(subscription)) return;
            }

            ISubscriber subscriber = getValidSubscriber(clientId);
            subscriber.notify(contextMessage);
            this.subscriptionRepository.deleteById(subscription.getId());
        });
    }

    private boolean priceVariationIsValidToNotify(SubscriptionModel subscriptionModel) {
        AssetModel assetModel = this.assetRepository.findById(subscriptionModel.getAssetId())
                .orElseThrow(AssetNotFoundException::new);

        double oldPrice = subscriptionModel.getQuotationAtMoment();
        double currentPrice = assetModel.getQuotation();
        return oldPrice * 1.1 < currentPrice || oldPrice * 0.9 > currentPrice;
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

    private void validateAssetExists(UUID assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new AssetNotFoundException("Asset not found with ID " + assetId);
        }
    }

    private void validateClientExists(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientIdNotFoundException(clientId);
        }
    }

    private void validateAssetIsInactive(AssetModel assetModel) {
        if (assetModel.isActive()) throw new AssetIsAlreadyActive();
    }

    private void validateAssetIsActive(AssetModel assetModel) {
        if (!assetModel.isActive()) throw new AssetIsInactive();
    }

    private void validateAssetIsStockOrCrypto(AssetModel assetModel) {
        System.out.println(assetModel.getAssetType().getName());
        if (!assetModel.getAssetType().getName().equals(AssetTypeEnum.STOCK.name()) && !assetModel.getAssetType().getName().equals(AssetTypeEnum.CRYPTO.name())) {
            throw new AssetIsNotStockNeitherCrypto();
        }
    }

    private void validateClient(UUID subscriberId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO, SubscriptionTypeEnum subscriptionType) {
        this.validateClientExists(subscriberId);

        ClientModel clientModel = this.clientRepository.findById(subscriberId)
                .orElseThrow(() -> new ClientIdNotFoundException(subscriberId));

        clientModel.validateAccess(clientMarkInterestInAssetRequestDTO.getAccessCode());

        if (subscriptionType == SubscriptionTypeEnum.PRICE_VARIATION) {
            if (clientModel.getPlanType() != PlanTypeEnum.PREMIUM) throw new ClientIsNotPremium();
        }
    }

    private void validateAsset(UUID assetId, SubscriptionTypeEnum subscriptionType) {
        this.validateAssetExists(assetId);

        AssetModel assetModel = this.assetRepository.findById(assetId).
                orElseThrow(AssetNotFoundException::new);

        if (subscriptionType == SubscriptionTypeEnum.AVAILABILITY) this.validateAssetIsInactive(assetModel);
        else {
            this.validateAssetIsActive(assetModel);
            this.validateAssetIsStockOrCrypto(assetModel);
        }
    }

    private double getQuotationAtMoment(UUID assetId) {
        AssetModel assetModel = this.assetRepository.findById(assetId).
                orElseThrow(AssetNotFoundException::new);

        return assetModel.getQuotation();
    }

    private ISubscriber getValidSubscriber(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(client -> (ISubscriber) client)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));
    }

}
