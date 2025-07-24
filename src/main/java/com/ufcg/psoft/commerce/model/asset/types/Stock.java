package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.AssetTypeEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STOCK")
public class Stock extends AssetType {

    private static final double TAX = 0.15;

    public Stock() {
        super(AssetTypeEnum.STOCK.name());
    }

    @Override
    public double taxCalculate(double profit) {
        return TAX * profit;
    }
}
