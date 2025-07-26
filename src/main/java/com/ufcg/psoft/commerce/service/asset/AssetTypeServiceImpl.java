package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetTypeServiceImpl implements AssetTypeService {

    @Autowired
    AssetTypeRepository assetTypeRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<AssetTypeResponseDTO> getAllAssetTypes() {
        List<AssetType> assetTypes = assetTypeRepository.findAll();
        return assetTypes.stream()
                .map(assetType -> modelMapper.map(assetType, AssetTypeResponseDTO.class))
                .collect(Collectors.toList());
    }
}
