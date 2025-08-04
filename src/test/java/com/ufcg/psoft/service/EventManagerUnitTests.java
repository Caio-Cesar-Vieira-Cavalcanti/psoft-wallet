package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsAlreadyActive;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactive;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIsNotPremium;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.model.asset.*;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.observer.SubscriptionRepository;
import com.ufcg.psoft.commerce.service.observer.EventManager;
import com.ufcg.psoft.commerce.service.observer.EventManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventManagerUnitTests {

    private AssetRepository assetRepository;
    private ClientRepository clientRepository;
    private SubscriptionRepository subscriptionRepository;

    private EventManager eventManager;

    private UUID assetId1;
    private UUID assetId2;
    private UUID clientId;

    @BeforeEach
    void setup() {
        assetRepository = mock(AssetRepository.class);
        clientRepository = mock(ClientRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);

        eventManager = new EventManagerImpl();
        ReflectionTestUtils.setField(eventManager, "assetRepository", assetRepository);
        ReflectionTestUtils.setField(eventManager, "clientRepository", clientRepository);
        ReflectionTestUtils.setField(eventManager, "subscriptionRepository", subscriptionRepository);

        assetId1 = UUID.randomUUID();
        assetId2 = UUID.randomUUID();
        clientId = UUID.randomUUID();

        AssetModel mockAsset1 = AssetModel.builder()
                .id(assetId1)
                .name("Mock Asset 1")
                .isActive(true)
                .quotation(100.0)
                .assetType(new Stock())
                .build();

        AssetModel mockAsset2 = AssetModel.builder()
                .id(assetId2)
                .name("Mock Asset 2")
                .isActive(false)
                .quotation(100.0)
                .assetType(new Crypto())
                .build();

        ClientModel mockClient = new ClientModel(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                new WalletModel()
        );

        when(assetRepository.existsById(assetId1)).thenReturn(true);
        when(assetRepository.existsById(assetId2)).thenReturn(true);
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(assetRepository.findById(assetId1)).thenReturn(Optional.of(mockAsset1));
        when(assetRepository.findById(assetId2)).thenReturn(Optional.of(mockAsset2));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(mockClient));
        when(subscriptionRepository.save(any(SubscriptionModel.class))).thenAnswer(invocation -> {
            SubscriptionModel sub = invocation.getArgument(0);
            sub.setId(UUID.randomUUID());
            return sub;
        });
    }

    @Test
    @DisplayName("Should subscribe client to asset price variation successfully")
    void testSubscribeToAssetPriceVariation_Success() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("123456")
                .build();

        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.PRICE_VARIATION);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId1, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.PRICE_VARIATION, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should ignore subscription but return a String saying it was successful")
    void testSubscribeToAssetPriceVariation_SameSubscription() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("123456")
                .build();

        eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.PRICE_VARIATION);
        eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.PRICE_VARIATION);
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.PRICE_VARIATION);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId1, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.PRICE_VARIATION, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should throw exception because the access is inactive")
    void testSubscribeToAssetPriceVariation_AssetIsInactive() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId2)
                .accessCode("123456")
                .build();

        AssetIsInactive exception = assertThrows(AssetIsInactive.class, () -> {
            eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.PRICE_VARIATION);
        });
        assertEquals("Asset is inactive!", exception.getMessage());

    }

    @Test
    @DisplayName("Should throw exception because the access is inactive")
    void testSubscribeToAssetPriceVariation_ClientIsNotPremium() {
        UUID otherClientId = UUID.randomUUID();

        ClientModel otherMockClient = new ClientModel(
                otherClientId,
                "Rafael Barreto",
                new EmailModel("rafael@email.com"),
                new AccessCodeModel("654321"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.NORMAL,
                10000.0,
                new WalletModel()
        );

        when(clientRepository.existsById(otherClientId)).thenReturn(true);
        when(clientRepository.findById(otherClientId)).thenReturn(Optional.of(otherMockClient));

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("654321")
                .build();

        ClientIsNotPremium exception = assertThrows(ClientIsNotPremium.class, () -> {
            eventManager.subscribeToAssetEvent(dto, otherClientId, SubscriptionTypeEnum.PRICE_VARIATION);
        });
        assertEquals("Client is not Premium!", exception.getMessage());
    }

    @Test
    @DisplayName("Should subscribe client to asset availability successfully")
    void testSubscribeToAssetAvailability_Success() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId2)
                .accessCode("123456")
                .build();

        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId2, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.AVAILABILITY, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should ignore subscription but return a String saying it was successful")
    void testSubscribeToAssetAvailability_SameSubscription() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId2)
                .accessCode("123456")
                .build();

        eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);
        eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId2, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.AVAILABILITY, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should throw exception because the asset is already active")
    void testSubscribeToAssetAvailability_AssetIsActive() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("123456")
                .build();

        AssetIsAlreadyActive exception = assertThrows(AssetIsAlreadyActive.class, () -> {
            eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);
        });
        assertEquals("Asset is already active!", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception because the asset doesn't exist")
    void testSubscribeToAssetEvent_InvalidAssetId() {
        UUID invalidAssetId = UUID.randomUUID();

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(invalidAssetId)
                .accessCode("123456")
                .build();

        assertThrows(AssetNotFoundException.class, () -> {
            eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);
        });
    }

    @Test
    @DisplayName("Should throw exception because the client doesn't exist")
    void testSubscribeToAssetEvent_InvalidClientId() {
        UUID invalidClientId = UUID.randomUUID();

        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("123456")
                .build();

        assertThrows(ClientIdNotFoundException.class, () -> {
            eventManager.subscribeToAssetEvent(dto, invalidClientId, SubscriptionTypeEnum.AVAILABILITY);
        });
    }

    @Test
    @DisplayName("Should throw exception because the access code is invalid")
    void testSubscribeToAssetEvent_InvalidAccessCode() {
        ClientMarkInterestInAssetRequestDTO dto = ClientMarkInterestInAssetRequestDTO.builder()
                .assetId(assetId1)
                .accessCode("654321")
                .build();

        UnauthorizedUserAccessException exception = assertThrows(UnauthorizedUserAccessException.class, () -> {
            eventManager.subscribeToAssetEvent(dto, clientId, SubscriptionTypeEnum.AVAILABILITY);
        });
        assertEquals("Unauthorized client access: access code is incorrect", exception.getMessage());
    }
}


    /*
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
    */