package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.*;
import com.ufcg.psoft.commerce.model.ClientModel;
import com.ufcg.psoft.commerce.repository.ClientRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

}
