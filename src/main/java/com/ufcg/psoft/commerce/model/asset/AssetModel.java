package com.ufcg.psoft.commerce.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsAlreadyActive;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetIsNotStockNeitherCryptoException;
import com.ufcg.psoft.commerce.exception.notification.EventManagerNotSetException;
import com.ufcg.psoft.commerce.service.observer.EventManager;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "asset")
@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetModel {

    @JsonProperty("id")
    @Id
    @GeneratedValue
    private UUID id;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("assetType")
    @ManyToOne
    @JoinColumn(name = "asset_type_id", nullable = false)
    private AssetType assetType;

    @JsonProperty("description")
    @Column(nullable = false)
    private String description;

    @JsonProperty("isActive")
    @Column(nullable = false)
    private boolean isActive;

    @JsonProperty("quotation")
    @Column(nullable = false)
    private double quotation;

    @JsonProperty("quota_quantity")
    @Column(nullable = false)
    private double quotaQuantity;

    @Transient
    @Setter
    private EventManager eventManager;

    public SubscriptionResponseDTO subscribe(UUID clientId, SubscriptionTypeEnum type) {
        if (eventManager == null) {
            throw new EventManagerNotSetException();
        }

        this.validateAsset(type);

        return eventManager.subscribeToAssetEvent(this.id, clientId, type);
    }

    public void updateQuotation(double newQuotation) {
        double oldQuotation = this.quotation;
        this.quotation = newQuotation;

        double notificationThreshold = 0.10;

        double variation = Math.abs((newQuotation - oldQuotation) / oldQuotation);

        if (variation >= notificationThreshold && eventManager != null) {
            eventManager.notifySubscribersByType(this.id, SubscriptionTypeEnum.PRICE_VARIATION);
        }
    }

    public void changeActiveStatus(boolean newStatus) {
        boolean wasInactive = !this.isActive;
        this.isActive = newStatus;

        if (wasInactive && this.isActive && eventManager != null) {
            eventManager.notifySubscribersByType(this.id, SubscriptionTypeEnum.AVAILABILITY);
        }
    }

    private void validateAsset(SubscriptionTypeEnum subscriptionType) {

        if (subscriptionType == SubscriptionTypeEnum.AVAILABILITY) this.validateAssetIsInactive();
        else {
            this.validateAssetIsActive();
            this.validateAssetIsStockOrCrypto();
        }
    }

    private void validateAssetIsStockOrCrypto() {
        String assetTypeName = this.getAssetType().getName();
        AssetTypeEnum assetTypeEnum;

        try {
            assetTypeEnum = AssetTypeEnum.valueOf(assetTypeName);
        } catch (IllegalArgumentException e) {
            throw new AssetIsNotStockNeitherCryptoException(this.name);
        }

        if (assetTypeEnum != AssetTypeEnum.STOCK && assetTypeEnum != AssetTypeEnum.CRYPTO) {
            throw new AssetIsNotStockNeitherCryptoException(this.name);
        }
    }

    private void validateAssetIsInactive() {
        if (this.isActive()) throw new AssetIsAlreadyActive();
    }

    private void validateAssetIsActive() {
        if (!this.isActive()) throw new AssetIsInactiveException();
    }
}
