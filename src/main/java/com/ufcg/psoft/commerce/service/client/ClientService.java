package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientResponseDTO getClientById(UUID id);

    List<ClientResponseDTO> getClients();

    ClientResponseDTO create(ClientPostRequestDTO body);

    ClientResponseDTO remove(UUID id, ClientDeleteRequestDTO body);

    ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO body);

    ResponseEntity<WalletResponseDTO> getPurchaseHistory(UUID clientId);

    List<AssetResponseDTO> redirectGetActiveAssets(PlanTypeEnum planType);
}
