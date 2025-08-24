package com.ufcg.psoft.commerce.exception.asset;

public class AssetQuantityAvailableIsInsufficientException extends RuntimeException {
    public AssetQuantityAvailableIsInsufficientException(String assetName, double requested, double available) {
        super("Insufficient quantity for asset '" + assetName + "': requested " + requested + ", available " + available);
    }
}
