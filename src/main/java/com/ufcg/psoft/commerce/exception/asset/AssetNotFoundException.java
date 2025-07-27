package com.ufcg.psoft.commerce.exception.asset;

public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException() {
      super("Asset not found!");
    }
    public AssetNotFoundException(String message) {
        super(message);
    }
}
