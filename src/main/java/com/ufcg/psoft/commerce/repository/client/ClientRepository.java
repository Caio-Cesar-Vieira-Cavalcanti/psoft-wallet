package com.ufcg.psoft.commerce.repository.client;

import com.ufcg.psoft.commerce.model.user.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<ClientModel, UUID> {
    @Query(
            value = "SELECT client_id FROM client_interest_availability_assets WHERE asset_id = :assetId",
            nativeQuery = true
    )
    List<UUID> findClientIdsByInterestedAvailabilityAssetId(@Param("assetId") UUID assetId);
}
