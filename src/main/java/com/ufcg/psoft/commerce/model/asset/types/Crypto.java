package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import jakarta.persistence.Entity;

@Entity
public class Crypto extends AssetType {

    private static final double LOW_PROFIT_TAX = 0.15;
    private static final double HIGH_PROFIT_TAX = 0.225;

    @Override
    public double taxCalculate(double profit) {
        if (profit <= 5000) {
            return LOW_PROFIT_TAX * profit;
        }
        return HIGH_PROFIT_TAX * profit;
    }
}
