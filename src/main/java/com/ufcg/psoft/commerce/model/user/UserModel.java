package com.ufcg.psoft.commerce.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ufcg.psoft.commerce.exception.admin.UnauthorizedAdminAccessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Embedded
    private EmailModel email;

    @JsonIgnore
    @Embedded
    private AccessCodeModel accessCode;

    public UserModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.accessCode = accessCode;
    }

    public void validateUser(String email, String accessCode) {
        if (!this.email.matches(email) || !this.accessCode.matches(accessCode)) {
            throw new UnauthorizedAdminAccessException();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public EmailModel getEmail() {
        return email;
    }

    public AccessCodeModel getAccessCode() {
        return accessCode;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}