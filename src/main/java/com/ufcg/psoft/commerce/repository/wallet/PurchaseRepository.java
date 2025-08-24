package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<PurchaseModel, UUID> {
    List<PurchaseModel> findByWalletId(UUID walletId);
}
