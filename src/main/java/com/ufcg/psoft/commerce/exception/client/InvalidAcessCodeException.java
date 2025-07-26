package com.ufcg.psoft.commerce.exception.client;

public class InvalidAcessCodeException extends IllegalArgumentException {

    public InvalidAcessCodeException() {
        super("Access Code Invalid.");
    }
    public InvalidAcessCodeException(String message) {
        super(message);
    }
}