package com.ufcg.psoft.commerce.service.mapper;

import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.model.user.ClientModel;
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
        if (walletModel == null) {
            return null;
        }

        WalletResponseDTO dto = modelMapper.map(walletModel, WalletResponseDTO.class);

        if (walletModel.getPurchases() != null) {
            List<PurchaseResponseDTO> purchases = walletModel.getPurchases().stream()
                    .map(p -> modelMapper.map(p, PurchaseResponseDTO.class))
                    .sorted(Comparator.comparing(PurchaseResponseDTO::getDate).reversed())
                    .toList();
            dto.setPurchases(purchases);
        } else {
            dto.setPurchases(List.of());
        }

        return dto;
    }
}
