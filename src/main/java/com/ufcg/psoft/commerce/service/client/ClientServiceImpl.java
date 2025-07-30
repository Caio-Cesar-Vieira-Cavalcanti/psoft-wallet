package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.enums.AssetTypeEnum;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    AssetService assetService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public ClientResponseDTO getClientById(UUID id) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientIdNotFoundException(id));

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
    public void remove(UUID id, ClientDeleteRequestDTO clientDeleteRequestDTO) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientIdNotFoundException(id));

        client.validateAccess(clientDeleteRequestDTO.getAccessCode());

        clientRepository.delete(client);
    }

    @Override
    public ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientIdNotFoundException(id));

        client.validateAccess(clientPatchFullNameRequestDTO.getAccessCode());

        client.setFullName(clientPatchFullNameRequestDTO.getFullName());
        clientRepository.save(client);
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public List<AssetResponseDTO> redirectGetActiveAssets(UUID id, ClientActiveAssetsRequestDTO clientActiveAssetsRequestDTO) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientIdNotFoundException(id));

        client.validateAccess(clientActiveAssetsRequestDTO.getAccessCode());

        PlanTypeEnum planType = this.getClientById(id).getPlanType();

        if (planType == PlanTypeEnum.PREMIUM) {
            return assetService.getAvailableAssets();
        }

        AssetType assetType = assetService.getAssetType(AssetTypeEnum.TREASURY_BOUNDS);
        return assetService.getActiveAssetsByAssetType(assetType);
    }

    @Override
    public WalletResponseDTO getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {
        ClientModel client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));

        client.validateAccess(clientPurchaseHistoryRequestDTO.getAccessCode());

        return dtoMapperService.toWalletResponseDTO(client.getWallet());
    }

    @Override
    public ClientResponseDTO markInterestInAvailableAsset(UUID id, ClientMarkInterestInAssetRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ClientResponseDTO markInterestInNotAvailableAsset(UUID id, ClientMarkInterestInAssetRequestDTO requestDTO) {
        return null;
    }
}
