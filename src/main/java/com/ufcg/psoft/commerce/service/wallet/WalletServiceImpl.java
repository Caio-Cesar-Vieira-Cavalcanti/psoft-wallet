package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.exception.user.ClientBudgetIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    PurchaseService purchaseService;

    @Autowired
    WithdrawService withdrawService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public PurchaseModel redirectCreatePurchaseRequest(WalletModel wallet, AssetModel asset, Integer assetQuantity) {
        double purchasePrice = asset.getQuotation() * assetQuantity;
        this.validateWalletBudget(wallet, purchasePrice);

        return purchaseService.createPurchaseRequest(wallet, asset, purchasePrice, assetQuantity);
    }

    @Override
    public List<PurchaseResponseDTO> redirectGetPurchaseHistory(UUID walletId, ClientPurchaseHistoryRequestDTO dto) {
        return purchaseService.getPurchaseHistoryByWalletId(walletId)
                .stream()
                .filter(w -> dto.getAssetType() == null || w.getAsset().getAssetType().equals(dto.getAssetType()))
                .filter(w -> dto.getPurchaseState() == null || w.getStateEnum().equals(dto.getPurchaseState()))
                .filter(w -> dto.getDate() == null || w.getDate().equals(dto.getDate()))
                .map(dtoMapperService::toPurchaseResponseDTO)
                .sorted(Comparator.comparing(PurchaseResponseDTO::getDate).reversed())
                .toList();
    }

    @Override
    public HoldingModel findHoldingByAsset(WalletModel wallet, AssetModel asset) {
        if (wallet.getHoldings() == null || wallet.getHoldings().isEmpty()) {
            return null;
        }

        return wallet.getHoldings()
                .values()
                .stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElse(null);
    }

    @Override
    public PurchaseResponseDTO addPurchase(PurchaseModel purchase) {
        HoldingModel holdingModel = this.findHoldingByAsset(purchase.getWallet(), purchase.getAsset());
        PurchaseResponseAfterAddedInWalletDTO dto = purchaseService.addedInWallet(purchase, holdingModel);
        purchase.getWallet().getHoldings().put(dto.getHolding().getId(), dto.getHolding());
        this.walletRepository.save(purchase.getWallet());
        return new PurchaseResponseDTO(dto);
    }

    private void validateWalletBudget(WalletModel wallet, double purchasePrice) {
        if (wallet.getBudget() < purchasePrice) {
            throw new ClientBudgetIsInsufficientException(
                    "Client budget is " + wallet.getBudget() +
                            ", while the purchase price is " + purchasePrice +
                            ". Therefore, the budget is insufficient."
            );
        }
    }
}
