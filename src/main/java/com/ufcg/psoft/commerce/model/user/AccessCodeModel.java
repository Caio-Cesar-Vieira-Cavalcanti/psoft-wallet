package com.ufcg.psoft.commerce.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@NoArgsConstructor
public class AccessCodeModel {

    @Column(length = 6, nullable = false)
    private String accessCode;

    public AccessCodeModel(String accessCode) {
        if (!accessCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("The access code must contain exactly 6 digits.");
        }
        this.accessCode = accessCode;
    }

    public boolean matches(String accessCode) {
        return this.accessCode.equals(accessCode);
    }

    public String getAccessCode() {
        return accessCode;
    }
}
