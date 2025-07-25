package com.ufcg.psoft.commerce.exception.asset;

public class InvalidQuotationVariationException extends RuntimeException {

  public InvalidQuotationVariationException() {
    super("The quotation variation should be at least 1% between updates.");
  }
  public InvalidQuotationVariationException(String message) {
    super(message);
  }
}