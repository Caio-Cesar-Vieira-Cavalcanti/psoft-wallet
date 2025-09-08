package com.ufcg.psoft.commerce.service.mapper;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.*;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public WithdrawResponseDTO toWithdrawResponseDTO(WithdrawModel withdrawModel) {
        return WithdrawResponseDTO.builder()
                .withdrawId(withdrawModel.getId())
                .walletId(withdrawModel.getWallet().getId())
                .assetId(withdrawModel.getAsset().getId())
                .quantityWithdrawn(withdrawModel.getQuantity())
                .valueReceived(withdrawModel.getWithdrawValue())
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
                .totalValue(withdrawModel.getQuantity() * withdrawModel.getSellingPrice())  // esse valor total é com a taxa inclusa? no caso, seria o withdrawValue?
                .tax(withdrawModel.getTax())
                .date(withdrawModel.getDate())
                .state(withdrawModel.getStateEnum())
                .build();
    }

    public OperationReportResponseDTO toOperationPurchaseReportDTO(
            PurchaseModel purchase,
            ClientModel client,
            double quantity,
            double gross,
            Double tax,
            Double net,
            LocalDateTime occurredAt
    ) {
        return OperationReportResponseDTO.builder()
                .operationId(purchase.getId())
                .operationType(OperationTypeEnum.PURCHASE)
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? client.getFullName() : null)
                .assetId(purchase.getAsset().getId())
                .assetName(purchase.getAsset().getName())
                .assetType(AssetTypeEnum.valueOf(purchase.getAsset().getAssetType().getName()))
                .quantity(quantity)
                .gross(gross)
                .tax(tax)
                .net(net)
                .occurredAt(occurredAt)
                .build();
    }

    public OperationReportResponseDTO toOperationWithdrawReportDTO(
            WithdrawModel withdraw,
            ClientModel client,
            double quantity,
            double gross,
            Double tax,
            Double net,
            LocalDateTime occurredAt
    ) {
        return OperationReportResponseDTO.builder()
                .operationId(withdraw.getId())
                .operationType(OperationTypeEnum.WITHDRAW)
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? client.getFullName() : null)
                .assetId(withdraw.getAsset().getId())
                .assetName(withdraw.getAsset().getName())
                .assetType(AssetTypeEnum.valueOf(withdraw.getAsset().getAssetType().getName()))
                .quantity(quantity)
                .gross(gross)
                .tax(tax)
                .net(net)
                .occurredAt(occurredAt)
                .build();
    }
}
