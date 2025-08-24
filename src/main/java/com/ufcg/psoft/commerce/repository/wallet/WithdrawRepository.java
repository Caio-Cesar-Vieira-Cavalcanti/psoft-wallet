package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawRepository extends JpaRepository<WithdrawModel, UUID> {
}
