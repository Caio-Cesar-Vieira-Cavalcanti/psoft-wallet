package com.ufcg.psoft.commerce.service.mapper;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.*;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
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

    public WithdrawResponseDTO toWithdrawResponseDTO(WalletModel wallet,
                                                     AssetModel asset,
                                                     double quantityWithdrawn,
                                                     double valueReceived,
                                                     WithdrawStateEnum state) {
        return WithdrawResponseDTO.builder()
                .walletId(wallet.getId())
                .assetId(asset.getId())
                .quantityWithdrawn(quantityWithdrawn)
                .valueReceived(valueReceived)
                .newWalletBudget(wallet.getBudget())
                .state(state)
                .build();
    }

    public WithdrawResponseDTO toWithdrawResponseDTO(com.ufcg.psoft.commerce.model.wallet.WithdrawModel withdrawModel,
                                                     double valueReceived) {
        return WithdrawResponseDTO.builder()
                .withdrawId(withdrawModel.getId())
                .walletId(withdrawModel.getWallet().getId())
                .assetId(withdrawModel.getAsset().getId())
                .quantityWithdrawn(withdrawModel.getQuantity())
                .valueReceived(valueReceived)
                .newWalletBudget(withdrawModel.getWallet().getBudget())
                .state(withdrawModel.getStateEnum())
                .build();
    }

    public WithdrawHistoryResponseDTO toWithdrawHistoryResponseDTO(com.ufcg.psoft.commerce.model.wallet.WithdrawModel withdrawModel) {
        return WithdrawHistoryResponseDTO.builder()
                .withdrawId(withdrawModel.getId())
                .assetName(withdrawModel.getAsset().getName())
                .assetId(withdrawModel.getAsset().getId())
                .quantityWithdrawn(withdrawModel.getQuantity())
                .sellingPrice(withdrawModel.getSellingPrice())
                .totalValue(withdrawModel.getQuantity() * withdrawModel.getSellingPrice())  // esse valor total é com a taxa inclusa? se sim, é bom passar o valor com a taxa como parametro, e evitar logica de calculo no DTOMapper
                .tax(withdrawModel.getTax())
                .date(withdrawModel.getDate())
                .state(withdrawModel.getStateEnum())
                .build();
    }
}
