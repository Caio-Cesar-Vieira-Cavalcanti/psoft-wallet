package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;

import java.util.List;

public interface AssetTypeService {
    List<AssetTypeResponseDTO> getAllAssetTypes();
}
