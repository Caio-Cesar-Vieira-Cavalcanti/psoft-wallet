package com.ufcg.psoft.commerce.exception.asset;

public class AssetIsNotStockNeitherCrypto extends RuntimeException {
    public AssetIsNotStockNeitherCrypto() { super("Asset is not stock neither crypto!"); }
    public AssetIsNotStockNeitherCrypto(String message) {
        super(message);
    }
}
