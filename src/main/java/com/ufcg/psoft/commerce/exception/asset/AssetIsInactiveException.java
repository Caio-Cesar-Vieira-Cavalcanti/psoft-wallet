package com.ufcg.psoft.commerce.exception.asset;

public class AssetIsInactiveException extends RuntimeException {
    public AssetIsInactiveException() {
        super("Asset is inactive!");
    }
    public AssetIsInactiveException(String message) {
        super(message);
    }
}
