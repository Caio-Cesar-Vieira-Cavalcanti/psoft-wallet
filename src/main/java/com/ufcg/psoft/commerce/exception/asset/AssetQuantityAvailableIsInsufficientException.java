package com.ufcg.psoft.commerce.exception.asset;

public class AssetQuantityAvailableIsInsufficientException extends RuntimeException {
    public AssetQuantityAvailableIsInsufficientException() {
        super("The are not enough assets available for what was requested in the purchase!");
    }
}
