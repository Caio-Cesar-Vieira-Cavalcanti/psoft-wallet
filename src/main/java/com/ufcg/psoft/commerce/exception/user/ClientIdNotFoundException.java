package com.ufcg.psoft.commerce.exception.user;

import java.util.UUID;

public class ClientIdNotFoundException extends RuntimeException {

    public ClientIdNotFoundException() {
        super("Client ID not found!");
    }
    public ClientIdNotFoundException(UUID id) {
        super("Client not found with ID " + id);
    }
}