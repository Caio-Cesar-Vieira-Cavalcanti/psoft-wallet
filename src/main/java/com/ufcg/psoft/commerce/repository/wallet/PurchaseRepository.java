package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<PurchaseModel, UUID> {
    List<PurchaseModel> findByWalletId(UUID walletId);

    @Query("""
           SELECT p
             FROM PurchaseModel p
            WHERE (:walletId IS NULL OR p.wallet.id = :walletId)
              AND (:assetType IS NULL OR p.asset.assetType.name = :assetType)
              AND (:dateFrom IS NULL OR p.date >= :dateFrom)
              AND (:dateTo   IS NULL OR p.date <= :dateTo)
           """)
    List<PurchaseModel> findWithFilters(@Param("walletId") UUID walletId,
                                        @Param("assetType") String assetType,
                                        @Param("dateFrom") LocalDate dateFrom,
                                        @Param("dateTo") LocalDate dateTo);
}
