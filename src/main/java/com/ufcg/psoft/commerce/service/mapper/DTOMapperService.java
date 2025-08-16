package com.ufcg.psoft.commerce.service.mapper;

import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        return null;
    }

    public PurchaseResponseDTO toPurchaseResponseDTO(PurchaseModel purchaseModel) {
        PurchaseResponseDTO dto = modelMapper.map(purchaseModel, PurchaseResponseDTO.class);
        dto.setWalletId(purchaseModel.getWallet().getId());
        dto.setAssetId(purchaseModel.getAsset().getId());
        dto.setPurchaseState(purchaseModel.getStateEnum());

        return dto;
    }
}
