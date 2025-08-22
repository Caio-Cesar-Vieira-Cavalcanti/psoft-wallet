package com.ufcg.psoft.commerce.exception.purchase;

public class PurchaseStateNotInitializedException extends RuntimeException {
    public PurchaseStateNotInitializedException() {
        super("Purchase state has not been initialized.");
    }
}
