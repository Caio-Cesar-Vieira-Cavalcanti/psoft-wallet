package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import jakarta.persistence.Entity;

@Entity
public class Stock extends AssetType {

    private static final double TAX = 0.15;

    @Override
    public double taxCalculate(double profit) {
        return TAX * profit;
    }
}
