package com.ufcg.psoft.commerce.exception.client;

import java.util.UUID;

public class ClientIdNotFoundException extends RuntimeException {

    public ClientIdNotFoundException() {
        super("Client ID not found!");
    }
    public ClientIdNotFoundException(UUID id) {
        super("Customer with ID " + id + " not found!");
    }
}