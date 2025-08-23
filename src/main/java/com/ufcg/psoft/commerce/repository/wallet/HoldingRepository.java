package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HoldingRepository extends JpaRepository<HoldingModel, UUID> {
}
