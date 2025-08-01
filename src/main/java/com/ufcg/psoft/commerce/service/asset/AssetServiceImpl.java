package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.asset.*;

import com.ufcg.psoft.commerce.enums.SubscriptionTypeEnum;
import com.ufcg.psoft.commerce.exception.asset.*;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.repository.asset.AssetRepository;

import com.ufcg.psoft.commerce.repository.asset.AssetTypeRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.observer.EventManager;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    private static final double MIN_QUOTATION_VARIATION = 0.01;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    AssetTypeRepository assetTypeRepository;

    @Autowired
    EventManager assetEventManager;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AdminService adminService;

    @Override
    public AssetResponseDTO create(AssetPostRequestDTO assetPostRequestDTO) {
        this.adminService.validateAdmin(assetPostRequestDTO.getAdminEmail(), assetPostRequestDTO.getAdminAccessCode());

        AssetModel assetModel = modelMapper.map(assetPostRequestDTO, AssetModel.class);
        AssetType assetType = getAssetType(assetPostRequestDTO.getAssetType());
        assetModel.setAssetType(assetType);
        assetRepository.save(assetModel);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public void delete(UUID idAsset, AssetDeleteRequestDTO assetDeleteRequestDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found with ID " + idAsset));

        adminService.validateAdmin(assetDeleteRequestDTO.getAdminEmail(), assetDeleteRequestDTO.getAdminAccessCode());

        try {
            assetRepository.delete(assetModel);
        } catch (DataIntegrityViolationException ex) {
            throw new AssetReferencedInPurchaseException();
        }
    }

    @Override
    public List<AssetResponseDTO> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(assetModel -> modelMapper.map(assetModel, AssetResponseDTO.class))
                .toList();
    }

    @Override
    public AssetResponseDTO getAssetById(UUID idAsset) {
        AssetModel assetModel = assetRepository.findById(idAsset).orElseThrow(AssetNotFoundException::new);
        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public List<AssetResponseDTO> getAvailableAssets() {
        return assetRepository.findByIsActiveTrue().stream()
                .map(asset -> modelMapper.map(asset, AssetResponseDTO.class))
                .toList();
    }

    @Override
    public List<AssetResponseDTO> getActiveAssetsByAssetType(AssetType assetType) {
        return assetRepository.findByAssetType(assetType)
                .stream()
                .filter(AssetModel::isActive)
                .map(asset -> modelMapper.map(asset, AssetResponseDTO.class))
                .toList();
    }

    // Utility Method
    public AssetType getAssetType(AssetTypeEnum assetTypeEnum) {
        String assetType = assetTypeEnum.name();
        return assetTypeRepository.findByName(assetType).orElseThrow(() -> new AssetTypeNotFoundException(assetType));
    }

    @Override
    public AssetResponseDTO updateQuotation(UUID idAsset, AssetQuotationUpdateDTO assetQuotationUpdateDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found with ID " + idAsset));

        adminService.validateAdmin(assetQuotationUpdateDTO.getAdminEmail(), assetQuotationUpdateDTO.getAdminAccessCode());

        String assetType = assetModel.getAssetType().getClass().getSimpleName().toUpperCase();
        if (!assetType.equals(AssetTypeEnum.STOCK.name()) && !assetType.equals(AssetTypeEnum.CRYPTO.name())) throw new InvalidAssetTypeException();

        double currentQuotation = assetModel.getQuotation();
        double newQuotation = assetQuotationUpdateDTO.getQuotation();

        double variation = Math.abs((newQuotation - currentQuotation) / currentQuotation);
        if (variation < MIN_QUOTATION_VARIATION) throw new InvalidQuotationVariationException();

        assetModel.setQuotation(newQuotation);
        assetRepository.save(assetModel);

        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }


    private void notifyAvailabilitySubscribersOnActivation(boolean wasInactive, AssetModel asset) {
        if (wasInactive && asset.isActive()) {
            assetEventManager.notifySubscribersByType(asset.getId(), SubscriptionTypeEnum.AVAILABILITY);
        }
    }

    @Override
    public AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetActivationPatchRequestDTO assetPatchRequestDTO) {
        AssetModel assetModel = assetRepository.findById(idAsset)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found with ID " + idAsset));

        adminService.validateAdmin(assetPatchRequestDTO.getAdminEmail(), assetPatchRequestDTO.getAdminAccessCode());

        boolean wasInactive = !assetModel.isActive();

        assetModel.setActive(assetPatchRequestDTO.getIsActive());
        assetRepository.save(assetModel);

        this.notifyAvailabilitySubscribersOnActivation(wasInactive, assetModel);

        return new AssetResponseDTO(assetModel);
    }
}
