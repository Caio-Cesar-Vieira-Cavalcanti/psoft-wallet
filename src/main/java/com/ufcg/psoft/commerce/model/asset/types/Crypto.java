package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRYPTO")
public class Crypto extends AssetType {

    private static final double LOW_PROFIT_TAX = 0.15;
    private static final double HIGH_PROFIT_TAX = 0.225;
    private static final double TAX_PROFIT_THRESHOLD = 5000;

    public Crypto() {
        super(AssetTypeEnum.CRYPTO.name());
    }

    @Override
    public double taxCalculate(double profit) {
        if (profit <= TAX_PROFIT_THRESHOLD) {
            return LOW_PROFIT_TAX * profit;
        }
        return HIGH_PROFIT_TAX * profit;
    }
}
