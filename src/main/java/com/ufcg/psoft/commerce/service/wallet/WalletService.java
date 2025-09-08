package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;


public interface WalletService {
    HoldingModel findHoldingByAsset(WalletModel wallet, AssetModel asset);
    PurchaseResponseDTO addPurchase(PurchaseModel purchase);
    PurchaseResponseAfterAddedInWalletDTO addedInWallet(PurchaseModel purchase, HoldingModel holdingModel);
    void processWithdrawInWallet(HoldingModel holding, WalletModel wallet, AssetModel asset, double quantityToWithdraw, double withdrawValue);
}
