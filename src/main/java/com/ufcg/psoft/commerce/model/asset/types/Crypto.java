package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import jakarta.persistence.Entity;

@Entity
public class Crypto extends AssetType {

    private static final double TAX_1 = 0.15;
    private static final double TAX_2 = 0.225;

    @Override
    public double taxCalculate(double profit) {
        if (profit <= 5000) {
            return TAX_1 * profit;
        }
        return TAX_2 * profit;
    }
}
