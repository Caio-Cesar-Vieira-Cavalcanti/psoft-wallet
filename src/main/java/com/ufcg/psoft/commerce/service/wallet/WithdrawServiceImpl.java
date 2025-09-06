package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientWithdrawAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.exception.withdraw.WithdrawNotFoundException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WithdrawServiceImpl implements WithdrawService {

    private static final double EMPTY_HOLDING = 0.0;
    private static final double MIN_TAXABLE_PROFIT = 0.0;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public WithdrawResponseDTO withdrawAsset(WalletModel wallet, AssetModel asset, double quantityToWithdraw) {
        HoldingModel holding = findHolding(wallet, asset);
        holding.validateQuantityToWithdraw(quantityToWithdraw);

        double tax = calculateWithdrawTax(holding, asset, quantityToWithdraw);

        double withdrawValue = calculateWithdrawValue(asset, quantityToWithdraw, tax);

        WithdrawModel withdrawModel = WithdrawModel.builder()
                .asset(asset)
                .wallet(wallet)
                .quantity(quantityToWithdraw)
                .date(LocalDate.now())
                .sellingPrice(asset.getQuotation())
                .tax(tax)
                .withdrawValue(withdrawValue)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();

        withdrawRepository.save(withdrawModel);

        return dtoMapperService.toWithdrawResponseDTO(withdrawModel);
    }

    @Override
    public WithdrawResponseDTO confirmWithdraw(UUID withdrawId, WithdrawConfirmationRequestDTO withdrawConfirmationRequestDTO) {
        WithdrawModel withdraw = withdrawRepository.findById(withdrawId)
                .orElseThrow(() -> new WithdrawNotFoundException(withdrawId));

        AdminModel admin = adminService.getAdmin();
        admin.validateAccess(withdrawConfirmationRequestDTO.getAdminEmail(), withdrawConfirmationRequestDTO.getAdminAccessCode());

        // First modification: REQUESTED -> CONFIRMED
        withdraw.modify(admin);
        withdrawRepository.save(withdraw);

        // Second modification: CONFIRMED -> IN_ACCOUNT (auto)
        withdraw.modify(admin);
        withdrawRepository.save(withdraw);

       processWithdraw(withdraw.getWallet(), withdraw.getAsset(), withdraw.getQuantity(), withdraw.getWithdrawValue());

        return dtoMapperService.toWithdrawResponseDTO(withdraw);
    }

    @Override
    public List<WithdrawHistoryResponseDTO> getWithdrawHistory(UUID walletId, ClientWithdrawHistoryRequestDTO dto) {
        return withdrawRepository.findByWalletId(walletId)
                .stream()
                .filter(w -> dto.getAssetType() == null || w.getAsset().getAssetType().equals(dto.getAssetType()))
                .filter(w -> dto.getWithdrawState() == null || w.getStateEnum().equals(dto.getWithdrawState()))
                .filter(w -> dto.getDate() == null || w.getDate().equals(dto.getDate()))
                .map(dtoMapperService::toWithdrawHistoryResponseDTO)
                .sorted((w1, w2) -> w2.getDate().compareTo(w1.getDate()))
                .toList();
    }

    @Override
    public WithdrawResponseDTO withdrawClientAsset(UUID clientId, UUID assetId, ClientWithdrawAssetRequestDTO dto) {
        ClientModel client = validateWithdrawClientAccess(clientId, dto.getAccessCode());

        WalletModel wallet = client.getWallet();
        AssetModel asset = assetService.fetchAsset(assetId);

        return this.withdrawAsset(wallet, asset, dto.getQuantityToWithdraw());
    }

    @Override
    public List<WithdrawHistoryResponseDTO> redirectGetWithdrawHistory(UUID clientId, ClientWithdrawHistoryRequestDTO dto) {
        ClientModel client = validateWithdrawClientAccess(clientId, dto.getAccessCode());
        return this.getWithdrawHistory(client.getWallet().getId(), dto);
    }

    private HoldingModel findHolding(WalletModel wallet, AssetModel asset) {
        return wallet.getHoldings()
                .values()
                .stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElseThrow(() -> new ClientHoldingIsInsufficientException(
                        "Client does not own asset " + asset.getName()
                ));
    }

    private void processWithdraw(WalletModel wallet, AssetModel asset, double quantityToWithdraw, double withdrawValue) {
        HoldingModel holding = findHolding(wallet, asset);

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

    private double calculateWithdrawTax(HoldingModel holding, AssetModel asset, double quantityToWithdraw) {
        double avgCost = holding.getAccumulatedPrice() / holding.getQuantity();
        double costBasis = avgCost * quantityToWithdraw;

        double gross = asset.getQuotation() * quantityToWithdraw;
        double profit = gross - costBasis;

        // If negative profit, tax will be zero.
        double taxableProfit = Math.max(MIN_TAXABLE_PROFIT, profit);

        return asset.getAssetType().taxCalculate(taxableProfit);
    }

    private double calculateWithdrawValue(AssetModel asset, double quantityToWithdraw, double tax) {
        double gross = asset.getQuotation() * quantityToWithdraw;
        return gross - tax;
    }

    private ClientModel validateWithdrawClientAccess(UUID clientId, String accessCode) {
        ClientModel client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientIdNotFoundException(clientId));

        if (!client.getAccessCode().getAccessCode().equals(accessCode)) throw new UnauthorizedUserAccessException();

        return client;
    }
}
