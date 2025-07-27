package com.ufcg.psoft.commerce.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;

@Data
@Embeddable
@Builder
@NoArgsConstructor
public class EmailModel {

    @Column(unique = true, nullable = false)
    private String email;

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
