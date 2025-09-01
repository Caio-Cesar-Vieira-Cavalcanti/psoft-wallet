package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.exception.withdraw.WithdrawNotFoundException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawServiceImpl.class);

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public WithdrawResponseDTO withdrawAsset(WalletModel wallet, AssetModel asset, double quantityToWithdraw) {
        HoldingModel holding = findHolding(wallet, asset);
        holding.validateQuantityToWithdraw(quantityToWithdraw);

        double tax = 0.0; // Usar lógica para calcular

        WithdrawModel withdrawModel = WithdrawModel.builder()
                .asset(asset)
                .wallet(wallet)
                .quantity(quantityToWithdraw)
                .date(LocalDate.now())
                .sellingPrice(asset.getQuotation())
                .tax(tax)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();

        withdrawRepository.save(withdrawModel);

        return dtoMapperService.toWithdrawResponseDTO(withdrawModel, 0.0);
    }

    @Override
    public WithdrawResponseDTO confirmWithdraw(UUID withdrawId, WithdrawConfirmationRequestDTO withdrawConfirmationRequestDTO) {
        WithdrawModel withdraw = withdrawRepository.findById(withdrawId)
                .orElseThrow(() -> new WithdrawNotFoundException(withdrawId));

        AdminModel admin = adminService.getAdmin();
        admin.validateAccess(withdrawConfirmationRequestDTO.getAdminEmail(), withdrawConfirmationRequestDTO.getAdminAccessCode());

        // Primeira modificação: REQUESTED -> CONFIRMED
        withdraw.modify(admin);
        withdrawRepository.save(withdraw);

        // Segunda modificação: CONFIRMED -> IN_ACCOUNT (automática)
        withdraw.modify(admin);
        withdrawRepository.save(withdraw);

        double withdrawValue = processWithdraw(withdraw.getWallet(), withdraw.getAsset(), withdraw.getQuantity());

        return dtoMapperService.toWithdrawResponseDTO(withdraw, withdrawValue);
    }

    @Override
    public List<WithdrawHistoryResponseDTO> getWithdrawHistory(UUID walletId) {
        return withdrawRepository.findByWalletId(walletId)
                .stream()
                .map(dtoMapperService::toWithdrawHistoryResponseDTO)
                .sorted((w1, w2) -> w2.getDate().compareTo(w1.getDate())) // Ordenar por data decrescente
                .toList();
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

    private double processWithdraw(WalletModel wallet, AssetModel asset, double quantityToWithdraw) {
        HoldingModel holding = findHolding(wallet, asset);
        
        holding.decreaseQuantityAfterWithdraw(quantityToWithdraw);
        holding.decreaseAccumulatedPriceAfterWithdraw(quantityToWithdraw, asset.getQuotation());

        double withdrawValue = quantityToWithdraw * asset.getQuotation();
        wallet.increaseBudgetAfterWithdraw(withdrawValue);

        if (holding.getQuantity() == EMPTY_HOLDING) {
            wallet.getHoldings().remove(holding.getId());
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        walletRepository.save(wallet);
        
        return withdrawValue;
    }
}
