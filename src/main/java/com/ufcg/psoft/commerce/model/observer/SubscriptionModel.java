package com.ufcg.psoft.commerce.model.observer;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "Subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "subscriber_id", nullable = false)
    private UUID subscriberId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTypeEnum subscriptionType;
}

