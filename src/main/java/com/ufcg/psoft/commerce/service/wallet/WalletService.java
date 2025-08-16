package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    List<PurchaseResponseDTO> redirectGetPurchaseHistory(UUID walletId);
    PurchaseModel redirectCreatePurchaseRequest(WalletModel wallet, AssetModel asset, Integer assetQuantity);
}
