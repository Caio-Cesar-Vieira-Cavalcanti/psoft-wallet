package com.ufcg.psoft.commerce.exception.asset;

public class AssetIsNotStockNeitherCryptoException extends RuntimeException {
    public AssetIsNotStockNeitherCryptoException() { super("Asset is not stock neither crypto!"); }
    public AssetIsNotStockNeitherCryptoException(String message) {
        super(message);
    }
}
