package com.ufcg.psoft.commerce.model.user;

import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue("A")
public class AdminModel extends UserModel {
    public AdminModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode) {
        super(id, fullName, email, accessCode);
    }

    @Override
    public void validateAccess(String email, String accessCode) {
        if (!this.getEmail().matches(email) || this.getAccessCode().matches(accessCode)) {
            throw new UnauthorizedUserAccessException("Unauthorized admin access: email or access code is incorrect");
        }
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
