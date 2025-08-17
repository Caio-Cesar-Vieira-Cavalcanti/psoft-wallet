package com.ufcg.psoft.commerce.exception.asset;

public class AssetIsAlreadyActive extends RuntimeException {
    public AssetIsAlreadyActive() { super("Asset is already active!"); }
}
