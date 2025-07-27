package com.ufcg.psoft.commerce.repository.asset;

import com.ufcg.psoft.commerce.model.asset.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {
    Optional<AssetType> findByName(String name);
}
