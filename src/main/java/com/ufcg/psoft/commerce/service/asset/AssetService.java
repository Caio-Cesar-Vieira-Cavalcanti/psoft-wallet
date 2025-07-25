package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO);

    List<AssetResponseDTO> getAllAssets();

    AssetResponseDTO getAssetById(UUID idAsset);

    AssetResponseDTO update(UUID idAsset, AssetPatchRequestDTO assetPatchRequestDTO);

    void delete(UUID idAsset);

    AssetResponseDTO updateQuotation(UUID idAsset, AssetQuotationUpdateDTO assetQuotationUpdateDTO);

    AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetStatusPatchDTO assetPatchRequestDTO);

}
