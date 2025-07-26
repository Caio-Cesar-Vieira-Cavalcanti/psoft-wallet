package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.client.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.client.InvalidAcessCodeException;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.AssetTypeEnum;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.ClientRepository;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    AssetService assetService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ClientResponseDTO getClientById(UUID id) {
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new ClientIdNotFoundException(id));
        return modelMapper.map(client, ClientResponseDTO.class);
    }

    @Override
    public List<ClientResponseDTO> getClients() {
        List<ClientModel> clients = clientRepository.findAll();
        return clients.stream()
                .map(client -> modelMapper.map(client, ClientResponseDTO.class))
                .toList();
    }

    @Override
    public ClientResponseDTO create(ClientPostRequestDTO body) {
        AddressModel addressModel = modelMapper.map(body.getAddress(), AddressModel.class);
        ClientModel clientModel = new ClientModel(
                UUID.randomUUID(),
                body.getFullName(),
                new EmailModel(body.getEmail()),
                new AccessCodeModel(body.getAccessCode()),
                addressModel,
                body.getPlanType(),
                body.getBudget(),
                null
        );
        ClientModel savedClient = clientRepository.save(clientModel);
        return modelMapper.map(savedClient, ClientResponseDTO.class);
    }

    @Override
    public ClientResponseDTO remove(UUID id, ClientDeleteRequestDTO body) {
        if (!this.isAccessCodeValid(id, body.getAccessCode())) {
            throw new InvalidAcessCodeException();
        }
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new ClientIdNotFoundException(id));
        clientRepository.delete(client);
        return modelMapper.map(client, ClientResponseDTO.class);
    }

    @Override
    public ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO body) {
        if (!this.isAccessCodeValid(id, body.getAccessCode())) {
            throw new InvalidAcessCodeException();
        }
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new ClientIdNotFoundException(id));
        client.setFullName(body.getFullName());
        clientRepository.save(client);
        return modelMapper.map(client, ClientResponseDTO.class);
    }

    private boolean isAccessCodeValid(UUID id, String accessCode) {
        AccessCodeModel a = new AccessCodeModel(accessCode);
        ClientModel client = clientRepository.findById(id).orElseThrow(() -> new ClientIdNotFoundException(id));
        return client.getAccessCode().equals(a);
    }

    @Override
    public List<AssetResponseDTO> redirectGetActiveAssets(PlanTypeEnum planType) {
        if (planType == PlanTypeEnum.PREMIUM) {
            return this.assetService.getActiveAssets();
        }

        AssetType assetType = assetService.getAssetType(AssetTypeEnum.TREASURY_BOUNDS);
        return this.assetService.getActiveAssetsByAssetType(assetType);
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
