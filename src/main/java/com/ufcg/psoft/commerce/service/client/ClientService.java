package com.ufcg.psoft.commerce.service.client;

import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletHoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientResponseDTO create(ClientPostRequestDTO clientPostRequestDTO);

    void remove(UUID id, ClientDeleteRequestDTO clientDeleteRequestDTO);

    ClientResponseDTO getClientById(UUID id);

    List<ClientResponseDTO> getClients();

    ClientResponseDTO patchFullName(UUID id, ClientPatchFullNameRequestDTO clientPatchFullNameRequestDTO);

    AssetResponseDTO redirectGetAssetDetails(UUID clientId, UUID assetId, ClientAssetAccessRequestDTO clientAssetAccessRequestDTO);

    List<AssetResponseDTO> redirectGetActiveAssets(UUID clientId, ClientActiveAssetsRequestDTO requestDTO);

    SubscriptionResponseDTO redirectMarkAvailabilityOfInterestInAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO);

    SubscriptionResponseDTO redirectMarkInterestInPriceVariationOfAsset(UUID clientId, ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO);

    PurchaseResponseDTO purchaseRequestForAvailableAsset(UUID clientId, UUID assetId, ClientPurchaseAssetRequestDTO dto);

    List<PurchaseResponseDTO> redirectGetPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO);

    ClientModel validateClientAccess(UUID clientId, String accessCode);

    PurchaseResponseDTO purchaseConfirmationByClient(UUID purchaseId, UUID clientId, PurchaseConfirmationByClientDTO dto);

    WalletHoldingResponseDTO getClientWalletHolding(UUID clientId, ClientWalletRequestDTO clientWalletRequestDTO);

    WithdrawResponseDTO withdrawClientAsset(UUID clientId, UUID assetId, ClientWithdrawAssetRequestDTO dto);
    
    List<WithdrawHistoryResponseDTO> redirectGetWithdrawHistory(UUID clientId, ClientWithdrawHistoryRequestDTO clientWalletRequestDTO);
}
