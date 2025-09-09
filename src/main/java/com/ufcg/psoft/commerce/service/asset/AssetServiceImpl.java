package com.ufcg.psoft.commerce.service.asset;

import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.*;

import com.ufcg.psoft.commerce.dto.client.ClientMarkInterestInAssetRequestDTO;
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
        AssetModel assetModel = this.getAsset(idAsset);

        adminService.validateAdmin(assetDeleteRequestDTO.getAdminEmail(), assetDeleteRequestDTO.getAdminAccessCode());

        try {
            assetRepository.delete(assetModel);
        } catch (DataIntegrityViolationException ex) {
            throw new AssetReferencedInPurchaseException();
        }
    }

    @Override
    public AssetResponseDTO getAssetById(UUID idAsset) {
        AssetModel assetModel = this.getAsset(idAsset);
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

    @Override
    public AssetResponseDTO updateQuotation(UUID idAsset, AssetQuotationUpdateDTO assetQuotationUpdateDTO) {
        AssetModel assetModel = this.getAsset(idAsset);

        adminService.validateAdmin(assetQuotationUpdateDTO.getAdminEmail(), assetQuotationUpdateDTO.getAdminAccessCode());

        String assetType = assetModel.getAssetType().getClass().getSimpleName().toUpperCase();
        if (!assetType.equals(AssetTypeEnum.STOCK.name()) && !assetType.equals(AssetTypeEnum.CRYPTO.name())) throw new InvalidAssetTypeException();

        double currentQuotation = assetModel.getQuotation();
        double newQuotation = assetQuotationUpdateDTO.getQuotation();

        double variation = Math.abs((newQuotation - currentQuotation) / currentQuotation);
        if (variation < MIN_QUOTATION_VARIATION) throw new InvalidQuotationVariationException();

        assetModel.updateQuotation(newQuotation);
        assetRepository.save(assetModel);

        return modelMapper.map(assetModel, AssetResponseDTO.class);
    }

    @Override
    public AssetResponseDTO setIsActive(UUID idAsset, @Valid AssetActivationPatchRequestDTO assetPatchRequestDTO) {
        AssetModel assetModel = this.getAsset(idAsset);

        adminService.validateAdmin(assetPatchRequestDTO.getAdminEmail(), assetPatchRequestDTO.getAdminAccessCode());

        assetModel.changeActiveStatus(assetPatchRequestDTO.getIsActive());

        assetRepository.save(assetModel);
        return new AssetResponseDTO(assetModel);
    }

    @Override
    public SubscriptionResponseDTO subscribeToAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO, SubscriptionTypeEnum subscriptionType) {
        AssetModel asset = getAsset(clientMarkInterestInAssetRequestDTO.getAssetId());
        return asset.subscribe(clientId, subscriptionType);
    }

    @Override
    public AssetModel fetchAsset(UUID assetId) {
        return getAsset(assetId);
    }

    @Override
    public AssetType fetchAssetType(AssetTypeEnum assetTypeEnum) {
        return getAssetType(assetTypeEnum);
    }

    private AssetType getAssetType(AssetTypeEnum assetTypeEnum) {
        String assetType = assetTypeEnum.name();
        return assetTypeRepository.findByName(assetType)
                .orElseThrow(() -> new AssetTypeNotFoundException(assetType));
    }

    private AssetModel getAsset(UUID assetId) {
        AssetModel assetModel = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found with ID " +  assetId));
        assetModel.setEventManager(assetEventManager);
        return assetModel;
    }
}
