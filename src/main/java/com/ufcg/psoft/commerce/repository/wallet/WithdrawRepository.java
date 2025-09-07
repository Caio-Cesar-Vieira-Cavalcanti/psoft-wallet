package com.ufcg.psoft.commerce.repository.wallet;

import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WithdrawRepository extends JpaRepository<WithdrawModel, UUID> {
    List<WithdrawModel> findByWalletId(UUID walletId);

    @Query("""
           SELECT w
             FROM WithdrawModel w
            WHERE (:walletId IS NULL OR w.wallet.id = :walletId)
              AND (:assetType IS NULL OR w.asset.assetType.name = :assetType)
              AND (:dateFrom IS NULL OR w.date >= :dateFrom)
              AND (:dateTo   IS NULL OR w.date <= :dateTo)
           """)
    List<WithdrawModel> findWithFilters(@Param("walletId") UUID walletId,
                                        @Param("assetType") String assetType,
                                        @Param("dateFrom") LocalDate dateFrom,
                                        @Param("dateTo") LocalDate dateTo);
}
