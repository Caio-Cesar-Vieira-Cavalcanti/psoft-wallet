package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class UserModel {

    public UserModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.accessCode = accessCode;
    }

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

}
