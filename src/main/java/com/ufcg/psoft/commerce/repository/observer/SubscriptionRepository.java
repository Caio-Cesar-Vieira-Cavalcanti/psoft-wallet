package com.ufcg.psoft.commerce.repository.observer;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.model.observer.SubscriptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, UUID> {
    List<SubscriptionModel> findByAssetIdAndSubscriptionType(UUID assetId, SubscriptionTypeEnum subscriptionType);
}

