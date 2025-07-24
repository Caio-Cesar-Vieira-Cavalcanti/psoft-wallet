package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.config.PatchMapper;
import com.ufcg.psoft.commerce.dto.asset.AssetPatchRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetPostRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO) {
        AssetModel assetModel = modelMapper.map(assetPostRequestDTO, AssetModel.class);
        assetRepository.save(assetModel);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public AssetResponseDTO getAssetById(UUID idAsset) {
        AssetModel assetModel = assetRepository.findById(idAsset).orElseThrow(AssetNotFoundException::new);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public AssetResponseDTO update(UUID idAsset, AssetPatchRequestDTO assetPatchRequestDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset).orElseThrow(AssetNotFoundException::new);
        PatchMapper.mapNonNull(assetPatchRequestDTO, assetModel);
        assetRepository.save(assetModel);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public void delete(UUID idAsset) {
        AssetModel assetModel = assetRepository.findById(idAsset).orElseThrow(AssetNotFoundException::new);
        assetRepository.delete(assetModel);
    }

}
