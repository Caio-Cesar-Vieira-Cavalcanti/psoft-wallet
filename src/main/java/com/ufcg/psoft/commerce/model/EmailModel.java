package com.ufcg.psoft.commerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.apache.commons.validator.routines.EmailValidator;

@Data
@Embeddable
public class EmailModel {

    @Column(unique = true, nullable = false)
    private String email;

    protected EmailModel() {}

    public EmailModel(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.email = email;
    }

    public boolean matches(String email) {
        return this.email.equals(email);
    }

    public String getEmail() {
        return email;
    }
}
