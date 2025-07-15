package com.ufcg.psoft.commerce.model.asset.types;

import com.ufcg.psoft.commerce.model.asset.IAssetType;

public class Stock implements IAssetType {
    @Override
    public double taxCalculate(double profit) {
        // todo
        return 0;
    }
}
