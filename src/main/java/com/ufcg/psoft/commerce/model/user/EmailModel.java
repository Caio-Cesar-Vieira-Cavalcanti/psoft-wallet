package com.ufcg.psoft.commerce.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.apache.commons.validator.routines.EmailValidator;

@Data
@Embeddable
@Builder
@Getter
@NoArgsConstructor
public final class EmailModel {

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
}
