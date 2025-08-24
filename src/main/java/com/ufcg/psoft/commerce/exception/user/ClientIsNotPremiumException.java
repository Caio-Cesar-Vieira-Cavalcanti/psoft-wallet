package com.ufcg.psoft.commerce.exception.user;

public class ClientIsNotPremiumException extends RuntimeException {
    public ClientIsNotPremiumException() { super("Client is not Premium!"); }
}
