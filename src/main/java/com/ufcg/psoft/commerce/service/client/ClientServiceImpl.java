package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public ClientResponseDTO getClientById(UUID id) {
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente com ID " + id + " não encontrado"));
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public List<ClientResponseDTO> getClients() {
        List<ClientModel> clients = clientRepository.findAll();
        return clients.stream()
                .map(dtoMapperService::toClientResponseDTO)
                .toList();
    }

    @Override
    public ClientResponseDTO create(ClientPostRequestDTO clientPostRequestDTO) {
        AddressModel addressModel = modelMapper.map(clientPostRequestDTO.getAddress(), AddressModel.class);
        WalletModel walletModel = WalletModel.builder().build();
        ClientModel clientModel = new ClientModel(
                UUID.randomUUID(),
                clientPostRequestDTO.getFullName(),
                new EmailModel(clientPostRequestDTO.getEmail()),
                new AccessCodeModel(clientPostRequestDTO.getAccessCode()),
                addressModel,
                clientPostRequestDTO.getPlanType(),
                clientPostRequestDTO.getBudget(),
                walletModel
        );
        ClientModel savedClient = clientRepository.save(clientModel);
        return dtoMapperService.toClientResponseDTO(savedClient);
    }

    @Override
    public ClientResponseDTO remove(UUID id, ClientDeleteRequestDTO clientDeleteRequestDTO) {
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente com ID " + id + " não encontrado"));

        client.validateAccess(clientDeleteRequestDTO.getAccessCode());

        clientRepository.delete(client);
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO) {
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Client not found with id " + id));

        client.validateAccess(clientPatchFullNameRequestDTO.getAccessCode());

        client.setFullName(clientPatchFullNameRequestDTO.getFullName());
        clientRepository.save(client);
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public WalletResponseDTO getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {
        ClientModel client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id " + clientId));

        client.validateAccess(clientPurchaseHistoryRequestDTO.getAccessCode());

        return dtoMapperService.toWalletResponseDTO(client.getWallet());
    }

}
