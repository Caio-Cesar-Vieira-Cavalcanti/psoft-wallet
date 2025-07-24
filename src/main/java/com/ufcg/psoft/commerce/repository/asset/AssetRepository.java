package com.ufcg.psoft.commerce.repository.asset;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
}
