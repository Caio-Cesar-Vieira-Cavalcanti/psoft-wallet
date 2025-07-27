package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientResponseDTO getClientById(UUID id);

    List<ClientResponseDTO> getClients();

    ClientResponseDTO create(ClientPostRequestDTO clientPostRequestDTO);

    void remove(UUID id, ClientDeleteRequestDTO clientDeleteRequestDTO);

    ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO);

    WalletResponseDTO getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO);

    List<AssetResponseDTO> redirectGetActiveAssets(UUID clientId, ClientActiveAssetsRequestDTO requestDTO);
}
