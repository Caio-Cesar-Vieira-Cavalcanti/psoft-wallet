package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientWithdrawAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;

import java.util.List;
import java.util.UUID;

public interface WithdrawService {

    WithdrawResponseDTO withdrawAsset(WalletModel wallet, AssetModel asset, double quantityToWithdraw);
    
    WithdrawResponseDTO confirmWithdraw(UUID withdrawId, WithdrawConfirmationRequestDTO withdrawConfirmationRequestDTO);
    
    List<WithdrawHistoryResponseDTO> getWithdrawHistory(UUID walletId, ClientWithdrawHistoryRequestDTO withdrawHistoryRequestDTO);

    WithdrawResponseDTO withdrawClientAsset(UUID clientId, UUID assetId, ClientWithdrawAssetRequestDTO dto);

    List<WithdrawHistoryResponseDTO> redirectGetWithdrawHistory(UUID clientId, ClientWithdrawHistoryRequestDTO clientWalletRequestDTO);
}
