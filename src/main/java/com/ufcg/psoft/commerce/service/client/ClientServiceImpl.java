package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.enums.*;
import com.ufcg.psoft.commerce.dto.Subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
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
    public void remove(UUID clientId, ClientDeleteRequestDTO clientDeleteRequestDTO) {
        this.validateClientAccess(clientId, clientDeleteRequestDTO.getAccessCode());

        ClientModel client = this.getClient(clientId);

        clientRepository.delete(client);
    }

    @Override
    public ClientResponseDTO patchFullName(UUID clientId, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO) {
        this.validateClientAccess(clientId, clientPatchFullNameRequestDTO.getAccessCode());

        ClientModel client = this.getClient(clientId);

        client.setFullName(clientPatchFullNameRequestDTO.getFullName());
        clientRepository.save(client);
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public List<AssetResponseDTO> redirectGetActiveAssets(UUID clientId, ClientActiveAssetsRequestDTO clientActiveAssetsRequestDTO) {
        this.validateClientAccess(clientId, clientActiveAssetsRequestDTO.getAccessCode());

        ClientModel client = this.getClient(clientId);

        PlanTypeEnum planType = client.getPlanType();

        if (planType == PlanTypeEnum.PREMIUM) {
            return assetService.getAvailableAssets();
        }

        AssetType assetType = assetService.getAssetType(AssetTypeEnum.TREASURY_BOUNDS);
        return assetService.getActiveAssetsByAssetType(assetType);
    }

    @Override
    public SubscriptionResponseDTO redirectMarkAvailabilityOfInterestInAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO) {
        this.validateClientAccess(clientId, clientMarkInterestInAssetRequestDTO.getAccessCode());

        return assetService.subscribeToAsset(clientId, clientMarkInterestInAssetRequestDTO, SubscriptionTypeEnum.AVAILABILITY);
    }

    @Override
    public SubscriptionResponseDTO redirectMarkInterestInPriceVariationOfAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO) {
        this.validateClientAccess(clientId, clientMarkInterestInAssetRequestDTO.getAccessCode());

        return assetService.subscribeToAsset(clientId, clientMarkInterestInAssetRequestDTO, SubscriptionTypeEnum.PRICE_VARIATION);
    }

    @Override
    public WalletResponseDTO getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {
        this.validateClientAccess(clientId, clientPurchaseHistoryRequestDTO.getAccessCode());

        ClientModel client = this.getClient(clientId);

        return dtoMapperService.toWalletResponseDTO(client.getWallet());
    }

    @Override
    public AssetResponseDTO getAssetDetails(UUID clientId, UUID assetId, ClientAssetAccessRequestDTO clientAssetAccessRequestDTO) {
        this.validateClientAccess(clientId, clientAssetAccessRequestDTO.getAccessCode());

        return assetService.getAssetById(assetId);
    }

    @Override
    public void validateClientAccess(UUID clientId, String accessCode) {
        ClientModel client = this.getClient(clientId);
        client.validateAccess(accessCode);
    }

    private ClientModel getClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));
    }

}
