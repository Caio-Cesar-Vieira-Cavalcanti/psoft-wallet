package com.ufcg.psoft.commerce.exception.asset;

public class AssetIsInactive extends RuntimeException {
    public AssetIsInactive() {
        super("Asset is inactive!");
    }
    public AssetIsInactive(String message) {
        super(message);
    }
}
