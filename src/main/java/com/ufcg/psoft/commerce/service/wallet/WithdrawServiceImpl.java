package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WithdrawServiceImpl implements WithdrawService {

    private static final double EMPTY_HOLDING = 0.0;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private DTOMapperService dtoMapperService;

    @Override
    public WithdrawResponseDTO withdrawAsset(WalletModel wallet, AssetModel asset, double quantityToWithdraw) {
        HoldingModel holding = findHolding(wallet, asset);
        validateQuantity(holding, quantityToWithdraw);

        double withdrawValue = processWithdraw(wallet, holding, asset, quantityToWithdraw);

        walletRepository.save(wallet);

        return dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, withdrawValue);
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

    private void validateQuantity(HoldingModel holding, double quantityToWithdraw) {
        if (holding.getQuantity() < quantityToWithdraw) {
            throw new ClientHoldingIsInsufficientException(
                    "Holding quantity " + holding.getQuantity() +
                            " is less than requested withdrawal " + quantityToWithdraw
            );
        }
    }

    private double processWithdraw(WalletModel wallet, HoldingModel holding, AssetModel asset, double quantityToWithdraw) {
        holding.setQuantity(holding.getQuantity() - quantityToWithdraw);
        holding.setAccumulatedPrice(holding.getAccumulatedPrice() - (quantityToWithdraw * asset.getQuotation()));

        double withdrawValue = quantityToWithdraw * asset.getQuotation();

        wallet.setBudget(wallet.getBudget() + withdrawValue);

        if (holding.getQuantity() == EMPTY_HOLDING) {
            wallet.getHoldings().remove(holding.getId());
            holdingRepository.delete(holding);
        }
        else holdingRepository.save(holding);

        return withdrawValue;
    }
}
