package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletModel, UUID> {
}
