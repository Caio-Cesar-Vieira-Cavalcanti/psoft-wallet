package com.ufcg.psoft.commerce.exception.user;

public class ClientBudgetIsInsufficient extends RuntimeException {
    public ClientBudgetIsInsufficient() { super("Client budget is insufficient!"); }
    public ClientBudgetIsInsufficient(String message) {
        super(message);
    }
}
