package com.ufcg.psoft.commerce.exception.withdraw;

import java.util.UUID;

public class WithdrawNotFoundException extends RuntimeException {
    public WithdrawNotFoundException(UUID withdrawId) {
        super("Withdraw not found for ID: " + withdrawId);
    }
}
