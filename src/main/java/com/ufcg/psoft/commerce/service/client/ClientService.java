package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ResponseEntity<ClientResponseDTO> getClientById(UUID id);

    ResponseEntity<List<ClientResponseDTO>> getClients();

    ResponseEntity<ClientResponseDTO> create(ClientPostRequestDTO body);

    ResponseEntity<ClientResponseDTO> remove(UUID id, ClientDeleteRequestDTO body);

    ResponseEntity<ClientResponseDTO> patchFullName(UUID id, ClientPatchFullNameRequestDTO body);

    ResponseEntity<WalletResponseDTO> getPurchaseHistory(UUID clientId);
}
