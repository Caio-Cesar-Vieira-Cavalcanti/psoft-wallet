package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIsNotPremium;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        clientRepository = mock(ClientRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);

        eventManager = new EventManagerImpl();
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
        mockAsset1.setEventManager(eventManager);

        AssetModel mockAsset2 = AssetModel.builder()
                .id(assetId2)
                .name("Mock Asset 2")
                .isActive(false)
                .quotation(100.0)
                .assetType(new Crypto())
                .build();
        mockAsset2.setEventManager(eventManager);

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


        when(clientRepository.existsById(clientId)).thenReturn(true);
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
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(assetId1, clientId, SubscriptionTypeEnum.PRICE_VARIATION);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId1, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.PRICE_VARIATION, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should ignore subscription but return a String saying it was successful")
    void testSubscribeToAssetPriceVariation_SameSubscription() {
        eventManager.subscribeToAssetEvent(assetId1, clientId, SubscriptionTypeEnum.PRICE_VARIATION);
        eventManager.subscribeToAssetEvent(assetId1, clientId, SubscriptionTypeEnum.PRICE_VARIATION);
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(assetId1, clientId, SubscriptionTypeEnum.PRICE_VARIATION);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId1, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.PRICE_VARIATION, response.getSubscriptionType());
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

        ClientIsNotPremium exception = assertThrows(ClientIsNotPremium.class, () -> {
            eventManager.subscribeToAssetEvent(assetId1, otherClientId, SubscriptionTypeEnum.PRICE_VARIATION);
        });
        assertEquals("Client is not Premium!", exception.getMessage());
    }

    @Test
    @DisplayName("Should subscribe client to asset availability successfully")
    void testSubscribeToAssetAvailability_Success() {
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(assetId2, clientId, SubscriptionTypeEnum.AVAILABILITY);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId2, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.AVAILABILITY, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should ignore subscription but return a String saying it was successful")
    void testSubscribeToAssetAvailability_SameSubscription() {
        eventManager.subscribeToAssetEvent(assetId2, clientId, SubscriptionTypeEnum.AVAILABILITY);
        eventManager.subscribeToAssetEvent(assetId2, clientId, SubscriptionTypeEnum.AVAILABILITY);
        SubscriptionResponseDTO response = eventManager.subscribeToAssetEvent(assetId2, clientId, SubscriptionTypeEnum.AVAILABILITY);

        assertEquals("Subscription registered successfully", response.getMessage());
        assertEquals(clientId, response.getClientId());
        assertEquals(assetId2, response.getAssetId());
        assertEquals(SubscriptionTypeEnum.AVAILABILITY, response.getSubscriptionType());
    }

    @Test
    @DisplayName("Should throw exception because the client doesn't exist")
    void testSubscribeToAssetEvent_InvalidClientId() {
        UUID invalidClientId = UUID.randomUUID();

        assertThrows(ClientIdNotFoundException.class, () -> {
            eventManager.subscribeToAssetEvent(assetId1, invalidClientId, SubscriptionTypeEnum.AVAILABILITY);
        });
    }

    @Test
    @DisplayName("Should process price variation notifications for subscribed clients")
    void testPriceVariationNotification_ValidScenario() {
        SubscriptionModel subscription = SubscriptionModel.builder()
                .assetId(assetId1)
                .subscriberId(clientId)
                .subscriptionType(SubscriptionTypeEnum.PRICE_VARIATION)
                .build();

        when(subscriptionRepository.findByAssetIdAndSubscriptionType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION))
                .thenReturn(List.of(subscription));

        eventManager.notifySubscribersByType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION);

        verify(clientRepository, times(1)).findById(clientId);
        verify(subscriptionRepository, times(1)).deleteById(subscription.getId());
    }

    @Test
    @DisplayName("Should process availability notifications for inactive assets")
    void testAvailabilityNotification_ValidScenario() {
        SubscriptionModel subscription = SubscriptionModel.builder()
                .assetId(assetId2)
                .subscriberId(clientId)
                .subscriptionType(SubscriptionTypeEnum.AVAILABILITY)
                .build();

        when(subscriptionRepository.findByAssetIdAndSubscriptionType(assetId2, SubscriptionTypeEnum.AVAILABILITY))
                .thenReturn(List.of(subscription));

        eventManager.notifySubscribersByType(assetId2, SubscriptionTypeEnum.AVAILABILITY);

        verify(clientRepository, times(1)).findById(clientId);
        verify(subscriptionRepository, times(1)).deleteById(subscription.getId());
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void testNotification_ClientNotFound_ThrowsException() {
        UUID unknownClientId = UUID.randomUUID();
        SubscriptionModel subscription = SubscriptionModel.builder()
                .assetId(assetId1)
                .subscriberId(unknownClientId)
                .subscriptionType(SubscriptionTypeEnum.PRICE_VARIATION)
                .build();

        when(subscriptionRepository.findByAssetIdAndSubscriptionType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION))
                .thenReturn(List.of(subscription));
        when(clientRepository.findById(unknownClientId)).thenReturn(Optional.empty());

        assertThrows(ClientIdNotFoundException.class, () ->
                eventManager.notifySubscribersByType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION));
    }

    @Test
    @DisplayName("Should do nothing when no subscriptions exist")
    void testNotification_NoSubscriptions_SilentSkip() {
        when(subscriptionRepository.findByAssetIdAndSubscriptionType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION))
                .thenReturn(List.of());

        eventManager.notifySubscribersByType(assetId1, SubscriptionTypeEnum.PRICE_VARIATION);

        verify(clientRepository, never()).findById(any());
        verify(subscriptionRepository, never()).deleteById(any());
    }
}