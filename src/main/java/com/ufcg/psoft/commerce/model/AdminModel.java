package com.ufcg.psoft.commerce.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.UUID;

@Entity
@DiscriminatorValue("A")
public class AdminModel extends UserModel {
    public AdminModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode) {
        super(id, fullName, email, accessCode);
    }
}
