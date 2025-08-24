package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    AdminService adminService;

    @Autowired
    HoldingRepository holdingRepository;

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
                .build();

        return purchaseRepository.save(purchaseModel);
    }

    @Override
    public PurchaseResponseDTO confirmAvailability(UUID purchaseId, PurchaseConfirmationRequestDTO purchaseConfirmationRequestDTO) {
        PurchaseModel purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        AdminModel admin = this.adminService.getAdmin();

        admin.validateAccess(purchaseConfirmationRequestDTO.getAdminEmail(), purchaseConfirmationRequestDTO.getAdminAccessCode());
        purchase.modify(admin);

        this.purchaseRepository.save(purchase);

        return new PurchaseResponseDTO(purchase);
    }

    @Override
    public PurchaseModel confirmationByClient(UUID purchaseId) {
        PurchaseModel purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        purchase.modify(null);

        this.purchaseRepository.save(purchase);

        return purchase;
    }

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

}
