package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;

import java.util.UUID;
import java.util.List;

public interface PurchaseService {
    List<PurchaseModel> getPurchaseHistoryByWalletId(UUID walletId);

    PurchaseModel createPurchaseRequest(WalletModel wallet, AssetModel asset, double purchasePrice, Integer assetQuantity);

    PurchaseResponseDTO confirmAvailability(UUID purchaseId, PurchaseConfirmationRequestDTO purchaseConfirmationRequestDTO);
}
