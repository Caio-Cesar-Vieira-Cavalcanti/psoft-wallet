package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.wallet.*;
import com.ufcg.psoft.commerce.enums.*;
import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.wallet.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    AssetService assetService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    DTOMapperService dtoMapperService;

    @Autowired
    TransactionService transactionService;

    @Override
    public ClientResponseDTO create(ClientPostRequestDTO clientPostRequestDTO) {
        AddressModel addressModel = modelMapper.map(clientPostRequestDTO.getAddress(), AddressModel.class);
        WalletModel walletModel = WalletModel.builder()
                .budget(clientPostRequestDTO.getBudget())
                .holdings(new HashMap<>())
                .build();

        ClientModel clientModel = new ClientModel(
                UUID.randomUUID(),
                clientPostRequestDTO.getFullName(),
                new EmailModel(clientPostRequestDTO.getEmail()),
                new AccessCodeModel(clientPostRequestDTO.getAccessCode()),
                addressModel,
                clientPostRequestDTO.getPlanType(),
                walletModel
        );
        ClientModel savedClient = clientRepository.save(clientModel);

        return dtoMapperService.toClientResponseDTO(savedClient);
    }

    @Override
    public void remove(UUID clientId, ClientDeleteRequestDTO clientDeleteRequestDTO) {
        ClientModel client = this.validateClientAccess(clientId, clientDeleteRequestDTO.getAccessCode());

        clientRepository.delete(client);
    }

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
    public ClientResponseDTO patchFullName(UUID clientId, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO) {
        ClientModel client = this.validateClientAccess(clientId, clientPatchFullNameRequestDTO.getAccessCode());
        client.setFullName(clientPatchFullNameRequestDTO.getFullName());

        clientRepository.save(client);
        return dtoMapperService.toClientResponseDTO(client);
    }

    @Override
    public AssetResponseDTO redirectGetAssetDetails(UUID clientId, UUID assetId, ClientAssetAccessRequestDTO clientAssetAccessRequestDTO) {
        this.validateClientAccess(clientId, clientAssetAccessRequestDTO.getAccessCode());

        return assetService.getAssetById(assetId);
    }

    @Override
    public List<AssetResponseDTO> redirectGetActiveAssets(UUID clientId, ClientActiveAssetsRequestDTO clientActiveAssetsRequestDTO) {
        ClientModel client = this.validateClientAccess(clientId, clientActiveAssetsRequestDTO.getAccessCode());
        PlanTypeEnum planType = client.getPlanType();

        if (planType == PlanTypeEnum.PREMIUM) {
            return assetService.getAvailableAssets();
        }

        AssetType assetType = assetService.fetchAssetType(AssetTypeEnum.TREASURY_BOUNDS);
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
    public ClientModel validateClientAccess(UUID clientId, String accessCode) {
        ClientModel client = this.getClient(clientId);
        client.validateAccess(accessCode);

        return client;
    }

    @Override
    public WalletHoldingResponseDTO getClientWalletHolding(UUID clientId, ClientWalletRequestDTO clientWalletRequestDTO) {
        ClientModel clientModel = this.validateClientAccess(clientId, clientWalletRequestDTO.getAccessCode());

        WalletModel walletModel = clientModel.getWallet();

        List<HoldingResponseDTO> holdings = buildHoldings(walletModel);

        double totalInvested   = calculateTotalInvested(holdings);
        double totalCurrent    = calculateTotalCurrent(holdings);
        double totalPerformance = totalCurrent - totalInvested;

        return dtoMapperService.toWalletHoldingResponseDTO(walletModel, holdings, totalCurrent, totalInvested, totalPerformance);
    }

    @Override
    public ClientExportTransactionsResponseDTO exportClientTransactionsCSV(UUID clientId, ClientExportTransactionsRequest dto) {
        return new ClientExportTransactionsResponseDTO();
    }

    private List<HoldingResponseDTO> buildHoldings(WalletModel walletModel) {
        if (walletModel.getHoldings() == null) {
            return List.of();
        }
        return walletModel.getHoldings()
                .values()
                .stream()
                .map(this::mapHoldingToDTO)
                .toList();
    }

    private HoldingResponseDTO mapHoldingToDTO(HoldingModel holding) {
        AssetModel asset = holding.getAsset();

        double quantity         = holding.getQuantity();
        double acquisitionTotal = holding.getAccumulatedPrice();
        double currentPrice     = asset.getQuotation();
        double currentTotal     = quantity * currentPrice;

        double performance      = currentTotal - acquisitionTotal;

        double acquisitionPrice = quantity == 0 ? 0 : (holding.getAccumulatedPrice()/quantity);

        return dtoMapperService.toHoldingResponseDTO(holding, asset, acquisitionPrice, acquisitionTotal, currentTotal, performance);
    }

    private double calculateTotalInvested(List<HoldingResponseDTO> holdings) {
        return holdings.stream().mapToDouble(HoldingResponseDTO::getAcquisitionTotal).sum();
    }

    private double calculateTotalCurrent(List<HoldingResponseDTO> holdings) {
        return holdings.stream().mapToDouble(HoldingResponseDTO::getCurrentTotal).sum();
    }

    private ClientModel getClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));
    }
}
