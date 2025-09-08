package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private static final double EMPTY_HOLDING = 0.0;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    HoldingRepository holdingRepository;

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
        PurchaseResponseAfterAddedInWalletDTO dto = this.addedInWallet(purchase, holdingModel);
        purchase.getWallet().getHoldings().put(holdingModel.getId(), holdingModel);
        this.walletRepository.save(purchase.getWallet());
        return new PurchaseResponseDTO(dto);
    }

    @Override
    public PurchaseResponseAfterAddedInWalletDTO addedInWallet(PurchaseModel purchase, HoldingModel holdingModel) {
        purchase.modify(null);
        this.purchaseRepository.save(purchase);

        if (holdingModel == null) {
            HoldingModel newHoldingModel = HoldingModel.builder()
                    .asset(purchase.getAsset())
                    .wallet(purchase.getWallet())
                    .quantity(purchase.getQuantity())
                    .accumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice())
                    .build();
            this.holdingRepository.save(newHoldingModel);
            return new PurchaseResponseAfterAddedInWalletDTO(purchase, newHoldingModel);
        } else {
            holdingModel.increaseQuantityAfterPurchase(purchase.getQuantity());
            holdingModel.increaseAccumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice());
        }
        return new PurchaseResponseAfterAddedInWalletDTO(purchase, holdingModel);
    }

    @Override
    public void processWithdrawInWallet(HoldingModel holding, WalletModel wallet, AssetModel asset, double quantityToWithdraw, double withdrawValue) {
        holding.decreaseQuantityAfterWithdraw(quantityToWithdraw);
        holding.decreaseAccumulatedPriceAfterWithdraw(quantityToWithdraw, asset.getQuotation());

        wallet.increaseBudgetAfterWithdraw(withdrawValue);

        if (holding.getQuantity() == EMPTY_HOLDING) {
            wallet.getHoldings().remove(holding.getId());
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        walletRepository.save(wallet);
    }
}
