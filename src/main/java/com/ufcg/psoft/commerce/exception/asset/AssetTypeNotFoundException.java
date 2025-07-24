package com.ufcg.psoft.commerce.exception.asset;

public class AssetTypeNotFoundException extends RuntimeException {
    public AssetTypeNotFoundException(String assetType) {
        super("Asset type:" + assetType + " not found!");
    }
}
