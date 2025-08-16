package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    PurchaseRepository purchaseRepository;

    @Override
    public List<PurchaseModel> getPurchaseHistoryByWalletId(UUID walletId) {
        return purchaseRepository.findByWalletId(walletId);
    }

    @Override
    public PurchaseModel createPurchaseRequest(WalletModel wallet, AssetModel asset, double purchasePrice, Integer assetQuantity) {

        PurchaseModel purchaseModel = PurchaseModel.builder()
                .asset(asset)
                .wallet(wallet)
                .quantity(assetQuantity)
                .date(LocalDate.now())
                .acquisitionPrice(purchasePrice)
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build();

        return purchaseRepository.save(purchaseModel);
    }
}
