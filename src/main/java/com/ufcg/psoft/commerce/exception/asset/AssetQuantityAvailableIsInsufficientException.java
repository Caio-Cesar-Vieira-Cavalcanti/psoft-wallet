package com.ufcg.psoft.commerce.exception.asset;

public class AssetQuantityAvailableIsInsufficient extends RuntimeException {
    public AssetQuantityAvailableIsInsufficient() {
        super("The are not enough assets available for what was requested in the purchase!");
    }
    public AssetQuantityAvailableIsInsufficient(String message) {
        super(message);
    }
}
