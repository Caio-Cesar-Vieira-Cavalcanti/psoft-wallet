package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.ClientRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ResponseEntity<ClientResponseDTO> getClientById(UUID id) {
        Optional<ClientModel> client = clientRepository.findById(id);
        if (client.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ClientResponseDTO dto = modelMapper.map(client.get(), ClientResponseDTO.class);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<List<ClientResponseDTO>> getClients() {
        List<ClientModel> clients = clientRepository.findAll();
        List<ClientResponseDTO> response = clients.stream()
                .map(client -> modelMapper.map(client, ClientResponseDTO.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientResponseDTO> create(ClientPostRequestDTO body) {
        ClientModel clientModel = modelMapper.map(body, ClientModel.class);
        ClientModel savedClient = clientRepository.save(clientModel);
        ClientResponseDTO response = modelMapper.map(savedClient, ClientResponseDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ClientResponseDTO> remove(UUID id, ClientDeleteRequestDTO body) {
        Optional<ClientModel> optionalClient = clientRepository.findById(id);

        if (optionalClient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClientModel client = optionalClient.get();
        if (!client.getAccessCode().getAccessCode().equals(body.getAccessCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        clientRepository.delete(client);
        ClientResponseDTO response = modelMapper.map(client, ClientResponseDTO.class);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientResponseDTO> patchFullName(UUID id, ClientPatchFullNameRequestDTO body) {
        Optional<ClientModel> optionalClient = clientRepository.findById(id);

        if (optionalClient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClientModel client = optionalClient.get();
        if (!client.getAccessCode().getAccessCode().equals(body.getAccessCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        client.setFullName(body.getFullName());
        ClientModel updatedClient = clientRepository.save(client);
        ClientResponseDTO response = modelMapper.map(updatedClient, ClientResponseDTO.class);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WalletResponseDTO> getPurchaseHistory(UUID clientId) {
        Optional<ClientModel> optionalClient = clientRepository.findById(clientId);

        if (optionalClient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClientModel client = optionalClient.get();
        WalletModel wallet = client.getWallet();

        if (wallet == null || wallet.getPurchases() == null || wallet.getPurchases().isEmpty()) {
            return ResponseEntity.ok(
                    WalletResponseDTO.builder()
                            .id(wallet != null ? wallet.getId() : null)
                            .purchases(List.of())
                            .build()
            );
        }

        List<PurchaseResponseDTO> purchases = wallet.getPurchases().values().stream()
                .sorted((p1, p2) -> p2.getDate().compareTo(p1.getDate()))
                .map(p -> PurchaseResponseDTO.builder()
                        .id(p.getId())
                        .assetId(p.getAsset().getId())
                        .quantity(p.getQuantity())
                        .state(p.getState())
                        .date(p.getDate())
                        .build()
                ).toList();

        WalletResponseDTO response = WalletResponseDTO.builder()
                .id(wallet.getId())
                .purchases(purchases)
                .build();

        return ResponseEntity.ok(response);
    }

}
