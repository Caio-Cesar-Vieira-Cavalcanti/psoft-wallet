package com.ufcg.psoft.commerce.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    @Setter
    private String fullName;

    @Embedded
    private EmailModel email;

    @JsonIgnore
    @Embedded
    private AccessCodeModel accessCode;

    protected UserModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.accessCode = accessCode;
    }

    public void validateAccess(String accessCode) {
        throw new UnsupportedOperationException("Access validation not supported for this user type");
    }

    public void validateAccess(String email, String accessCode) {
        throw new UnsupportedOperationException("Access validation not supported for this user type");
    }

    public abstract boolean isAdmin();
}
