package com.ufcg.psoft.commerce.exception.notification;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;

import java.util.UUID;

public class AlreadySubscribedException extends RuntimeException {
    public AlreadySubscribedException(UUID assetId, SubscriptionTypeEnum type) {
        super(String.format(
            "Client is already subscribed to asset %s for %s notifications",
            assetId, type
        ));
    }

}
