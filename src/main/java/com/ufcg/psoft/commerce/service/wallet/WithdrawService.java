package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;

public interface WithdrawService {

    WithdrawResponseDTO withdrawAsset(WalletModel wallet, AssetModel asset, double quantityToWithdraw);
}
