package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.*;
import com.ufcg.psoft.commerce.dto.client.ClientMarkInterestInAssetRequestDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO);

    void delete(UUID idAsset, AssetDeleteRequestDTO assetDeleteRequestDTO);

    List<AssetResponseDTO> getAvailableAssets();

    List<AssetResponseDTO> getActiveAssetsByAssetType(AssetType assetType);

    AssetResponseDTO getAssetById(UUID idAsset);

    AssetResponseDTO updateQuotation(UUID idAsset, AssetQuotationUpdateDTO assetQuotationUpdateDTO);

    AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetActivationPatchRequestDTO assetPatchRequestDTO);

    SubscriptionResponseDTO subscribeToAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO, SubscriptionTypeEnum subscriptionType);

    AssetType fetchAssetType(AssetTypeEnum assetTypeEnum);

    AssetModel fetchAsset(UUID assetId);
}
