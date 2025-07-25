package com.ufcg.psoft.commerce.exception.asset;

public class InvalidAssetTypeException extends IllegalArgumentException {

  public InvalidAssetTypeException() {
    super("Only Stock or Crypto assets can have the quotation updated.");
  }
  public InvalidAssetTypeException(String message) {
    super(message);
  }
}