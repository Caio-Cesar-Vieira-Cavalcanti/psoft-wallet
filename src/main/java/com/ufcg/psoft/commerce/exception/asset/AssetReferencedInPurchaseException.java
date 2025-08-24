package com.ufcg.psoft.commerce.exception.asset;

public class AssetReferencedInPurchaseException extends RuntimeException {
    public AssetReferencedInPurchaseException() {super("Cannot delete asset: it is referenced in purchases");}
}
