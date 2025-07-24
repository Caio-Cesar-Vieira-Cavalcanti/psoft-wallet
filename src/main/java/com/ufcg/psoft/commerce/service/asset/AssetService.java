package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.AssetPatchRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetPostRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO);

    List<AssetResponseDTO> getAllAssets();

    AssetResponseDTO getAssetById(UUID idAsset);

    AssetResponseDTO update(UUID idAsset, AssetPatchRequestDTO assetPatchRequestDTO);

    void delete(UUID idAsset);

}
