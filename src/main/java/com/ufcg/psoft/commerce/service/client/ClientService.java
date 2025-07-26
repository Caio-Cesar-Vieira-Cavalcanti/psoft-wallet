package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientResponseDTO getClientById(UUID id);

    List<ClientResponseDTO> getClients();

    ClientResponseDTO create(ClientPostRequestDTO clientPostRequestDTO);

    ClientResponseDTO remove(UUID id, ClientDeleteRequestDTO clientDeleteRequestDTO);

    ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO);

    WalletResponseDTO getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO);
}
