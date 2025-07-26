package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO);

    List<AssetResponseDTO> getAllAssets();

    AssetResponseDTO getAssetById(UUID idAsset);

    void delete(UUID idAsset, AssetDeleteRequestDTO assetDeleteRequestDTO);

    AssetResponseDTO updateQuotation(UUID idAsset, AssetQuotationUpdateDTO assetQuotationUpdateDTO);

    AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetActivationPatchRequestDTO assetPatchRequestDTO);

    List<AssetResponseDTO> getAvailableAssets();
}
