package com.ufcg.psoft.commerce.repository.asset;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
}
