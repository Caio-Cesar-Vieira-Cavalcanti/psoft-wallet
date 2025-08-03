package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.model.asset.*;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.observer.SubscriptionRepository;
import com.ufcg.psoft.commerce.service.observer.EventManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class EventManagerUnitTests {

    @Autowired
    private EventManagerImpl eventManager;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private AssetType stockType;
    private UUID clientId;
    private UUID assetId;

    @BeforeEach
    void setup() {
        ClientModel client = new ClientModel(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                new WalletModel()
        );

        clientId = clientRepository.save(client).getId();

        stockType = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("No STOCK asset found. Please ensure it's pre-populated for tests."));

        AssetModel asset = AssetModel.builder()
                .name("Default Asset Test")
                .isActive(false)
                .assetType(stockType)
                .description("Default asset for tests")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .build();

        assetId = assetRepository.save(asset).getId();

        SubscriptionModel subscription = new SubscriptionModel();
        subscription.setAssetId(assetId);
        subscription.setClientId(clientId);
        subscription.setQuotationAtMoment(asset.getQuotation());
        subscription.setSubscriptionType(SubscriptionTypeEnum.PRICE_VARIATION); //SubscriptionTypeEnum.AVAILABILITY

        subscriptionRepository.save(subscription);
    }

    /*
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
    }*/
}