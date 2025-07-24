package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.AssetTypeEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TREASURY_BOUNDS")
public class TreasuryBounds extends AssetType {

    private static final double TAX = 0.10;

    public TreasuryBounds() {
        super(AssetTypeEnum.TREASURY_BOUNDS.name());
    }

    @Override
    public double taxCalculate(double profit) {
        return TAX * profit;
    }
}
