package com.ufcg.psoft.commerce.service.report.fetchers;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.TransactionModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class OperationFetcherTemplate<T extends TransactionModel> implements OperationFetcher {

    @Autowired
    ClientRepository clientRepository;

    @Override
    public final List<OperationReportResponseDTO> fetch(OperationReportRequestDTO opRequestDTO) {
        if(!supportsOperationType(opRequestDTO.getOperationType())) return List.of();

        UUID walletIdFilter = resolveWalletId(opRequestDTO.getClientId());
        List<T> items = findItems(opRequestDTO, walletIdFilter);

        Map<UUID, ClientModel> walletToClient = walletIndex();

        return items.stream()
                .map(item -> map(item, walletToClient))
                .toList();
    }

    protected abstract boolean supportsOperationType(OperationTypeEnum operationTypeEnum);
    protected abstract List<T> findItems(OperationReportRequestDTO opRequestDTO, UUID walletIdFilter);
    protected abstract OperationReportResponseDTO map(T item, Map<UUID, ClientModel> walletToClient);


    protected LocalDateTime dateOf(T item) {
        LocalDate date = item.getDate();
        return (date != null) ? date.atStartOfDay() : LocalDateTime.MIN;
    }
    protected UUID walletIdOf(T item) {
        WalletModel wallet = item.getWallet();
        return (wallet != null) ? wallet.getId() : null;
    }

    private UUID resolveWalletId(UUID clientId) {
        if(clientId == null) return null;
        return clientRepository.findById(clientId)
                .map(c -> c.getWallet() != null ? c.getWallet().getId() : null)
                .orElse(null);
    }

    private Map<UUID, ClientModel> walletIndex() {
        return clientRepository.findAll().stream()
                .filter(c -> c.getWallet() != null && c.getWallet().getId() != null)
                .collect(Collectors.toMap(
                        c -> c.getWallet().getId(), Function.identity(), (a, b) -> a
                ));
    }
}
