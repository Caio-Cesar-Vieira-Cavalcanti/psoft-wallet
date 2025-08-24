package com.ufcg.psoft.commerce.service.mapper;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.HoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletHoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DTOMapperService {

    private final ModelMapper modelMapper;

    public ClientResponseDTO toClientResponseDTO(ClientModel clientModel) {
        ClientResponseDTO dto = modelMapper.map(clientModel, ClientResponseDTO.class);
        dto.setWallet(toWalletResponseDTO(clientModel.getWallet()));
        return dto;
    }

    public WalletResponseDTO toWalletResponseDTO(WalletModel walletModel) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(walletModel.getId());
        dto.setBudget(walletModel.getBudget());
        return dto;
    }

    public WalletHoldingResponseDTO toWalletHoldingResponseDTO(WalletModel walletModel, List<HoldingResponseDTO> holdings,
                                                               double totalCurrent,
                                                               double totalInvested,
                                                               double totalPerformance) {
        return WalletHoldingResponseDTO.builder()
                .walletResponseDTO(toWalletResponseDTO(walletModel))
                .holdings(holdings)
                .totalInvested(totalInvested)
                .totalCurrent(totalCurrent)
                .totalPerformance(totalPerformance)
                .build();
    }

    public PurchaseResponseDTO toPurchaseResponseDTO(PurchaseModel purchaseModel) {
        PurchaseResponseDTO dto = modelMapper.map(purchaseModel, PurchaseResponseDTO.class);
        dto.setWalletId(purchaseModel.getWallet().getId());
        dto.setAssetId(purchaseModel.getAsset().getId());
        dto.setPurchaseState(purchaseModel.getStateEnum());

        return dto;
    }

    public HoldingResponseDTO toHoldingResponseDTO(HoldingModel holdingModel, AssetModel assetModel,
                                                   double acquisitionPrice,
                                                   double acquisitionTotal,
                                                   double currentTotal,
                                                   double performance
                                                   ) {
        return HoldingResponseDTO.builder()
                .assetId(assetModel.getId())
                .assetName(assetModel.getName())
                .assetType(new AssetTypeResponseDTO(assetModel.getAssetType().getId(), assetModel.getAssetType().getName()))
                .quantity(holdingModel.getQuantity())
                .acquisitionPrice(acquisitionPrice)
                .currentPrice(assetModel.getQuotation())
                .performance(performance)
                .acquisitionTotal(acquisitionTotal)
                .currentTotal(currentTotal)
                .build();
    }
}
