package com.ufcg.psoft.commerce.exception.user;

public class ClientIsNotPremium extends RuntimeException {
    public ClientIsNotPremium() { super("Client is not Premium!"); }
    public ClientIsNotPremium(String message) {
        super(message);
    }
}
