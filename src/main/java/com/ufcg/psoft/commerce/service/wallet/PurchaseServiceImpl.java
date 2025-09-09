package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientPurchaseAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientBudgetIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    ClientService clientService;

    @Autowired
    AdminService adminService;

    @Autowired
    AssetService assetService;

    @Autowired
    WalletService walletService;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public List<PurchaseResponseDTO> getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO dto) {
        ClientModel client = clientService.validateClientAccess(clientId, dto.getAccessCode());
        UUID walletId = client.getWallet().getId();

        return purchaseRepository.findByWalletId(walletId)
                .stream()
                .filter(filter1 -> dto.getAssetType() == null || filter1.getAsset().getAssetType().getName().equals(dto.getAssetType().name()))
                .filter(filter2 -> dto.getPurchaseState() == null || filter2.getStateEnum().equals(dto.getPurchaseState()))
                .filter(filter3 -> dto.getDate() == null || filter3.getDate().equals(dto.getDate()))
                .map(dtoMapperService::toPurchaseResponseDTO)
                .sorted(Comparator.comparing(PurchaseResponseDTO::getDate).reversed())
                .toList();
    }

    @Override
    public PurchaseResponseDTO createPurchaseRequest(UUID clientId, UUID assetId, ClientPurchaseAssetRequestDTO dto) {
        ClientModel client = clientService.validateClientAccess(clientId, dto.getAccessCode());
        AssetModel asset = assetService.fetchAsset(assetId);
        asset.validateAssetIsActive();

        double purchasePrice = asset.getQuotation() * dto.getAssetQuantity();

        this.validateBudget(client.getWallet(), purchasePrice);

        PurchaseModel purchaseModel = PurchaseModel.builder()
                .asset(asset)
                .wallet(client.getWallet())
                .quantity(dto.getAssetQuantity())
                .date(LocalDate.now())
                .acquisitionPrice(purchasePrice)
                .build();

        purchaseRepository.save(purchaseModel);

        return dtoMapperService.toPurchaseResponseDTO(purchaseModel);
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
    public PurchaseResponseDTO confirmPurchase(UUID purchaseId, UUID clientId, PurchaseConfirmationByClientDTO dto) {
        ClientModel clientModel = clientService.validateClientAccess(clientId, dto.getAccessCode());

        PurchaseModel purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        purchase.modify(null);

        this.purchaseRepository.save(purchase);

        clientModel.getWallet().decreaseBudgetAfterPurchase(purchase.getAcquisitionPrice() * purchase.getQuantity());

        return walletService.addPurchase(purchase);
    }

    private void validateBudget(WalletModel wallet, double purchasePrice) {
        if (wallet.getBudget() < purchasePrice) {
            throw new ClientBudgetIsInsufficientException(
                    "Client budget is " + wallet.getBudget() +
                            ", while the purchase price is " + purchasePrice +
                            ". Therefore, the budget is insufficient."
            );
        }
    }
}
