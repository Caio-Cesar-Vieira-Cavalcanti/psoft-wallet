package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.config.PatchMapper;
import com.ufcg.psoft.commerce.dto.asset.AssetPatchRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetPostRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;

import com.ufcg.psoft.commerce.dto.asset.AssetStatusPatchDTO;
import com.ufcg.psoft.commerce.exception.asset.AssetTypeNotFoundException;
import com.ufcg.psoft.commerce.exception.asset.InvalidAssetTypeException;
import com.ufcg.psoft.commerce.exception.asset.InvalidQuotationVariationException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.AssetTypeEnum;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;
import com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException;

import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements AssetService {

    private static final double MIN_QUOTATION_VARIATION = 0.01;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    AssetTypeRepository assetTypeRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AdminService adminService;

    @Override
    public AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO) {
        AssetModel assetModel = modelMapper.map(assetPostRequestDTO, AssetModel.class);
        AssetType assetType = getAssetType(assetPostRequestDTO.getAssetType());
        assetModel.setAssetType(assetType);
        assetRepository.save(assetModel);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public List<AssetResponseDTO> getAllAssets() {
        List<AssetModel> assetModels = assetRepository.findAll();
        return assetModels.stream()
                .map(assetModel -> modelMapper.map(assetModel, AssetResponseDTO.class))
                .collect(Collectors.toList());
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

    private AssetType getAssetType(AssetTypeEnum assetTypeEnum) {
        String assetType = assetTypeEnum.name();
        return assetTypeRepository.findByName(assetType).orElseThrow(() -> new AssetTypeNotFoundException(assetType));
    }

    @Override
    public AssetResponseDTO updateQuotation(UUID idAsset, AssetPatchRequestDTO assetPatchRequestDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset)
                .orElseThrow(AssetNotFoundException::new);

        String assetType = assetModel.getAssetType().getClass().getSimpleName().toUpperCase();
        if (!assetType.equals(AssetTypeEnum.STOCK.name()) && !assetType.equals(AssetTypeEnum.CRYPTO.name())) throw new InvalidAssetTypeException();

        double currentQuotation = assetModel.getQuotation();
        double newQuotation = assetPatchRequestDTO.getQuotation();

        double variation = Math.abs((newQuotation - currentQuotation) / currentQuotation);
        if (variation < MIN_QUOTATION_VARIATION) throw new InvalidQuotationVariationException();

        assetModel.setQuotation(newQuotation);
        assetRepository.save(assetModel);

        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetStatusPatchDTO assetPatchRequestDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset).orElseThrow(AssetNotFoundException::new);

        adminService.validateAdmin(assetPatchRequestDTO.getAdminEmail(), assetPatchRequestDTO.getAdminAccessCode());

        assetModel.setActive(assetPatchRequestDTO.getIsActive());
        assetRepository.save(assetModel);
        return new AssetResponseDTO(assetModel);
    }

}
