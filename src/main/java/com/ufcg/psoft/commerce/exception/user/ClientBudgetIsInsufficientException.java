package com.ufcg.psoft.commerce.exception.user;

public class ClientBudgetIsInsufficientException extends RuntimeException {
    public ClientBudgetIsInsufficientException() { super("Client budget is insufficient!"); }
    public ClientBudgetIsInsufficientException(String message) {
        super(message);
    }
}
