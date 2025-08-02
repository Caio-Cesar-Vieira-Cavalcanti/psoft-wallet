package com.ufcg.psoft.commerce.model.observer;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "quotation_at_momemnt")
    private double quotationAtMoment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTypeEnum subscriptionType;
}

