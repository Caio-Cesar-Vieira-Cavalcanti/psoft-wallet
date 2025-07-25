package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.user.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<ClientModel, UUID> { }
