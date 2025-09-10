package com.ufcg.psoft.commerce.service.report.fetchers;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class WithdrawFetcher extends OperationFetcherTemplate<WithdrawModel> {

    @Autowired
    WithdrawRepository withdrawRepository;

    @Autowired
    DTOMapperService dtoMapperService;


    @Override
    protected boolean supportsOperationType(OperationTypeEnum operationTypeEnum) {
        return operationTypeEnum == null || operationTypeEnum == OperationTypeEnum.WITHDRAW;
    }

    @Override
    protected List<WithdrawModel> findItems(OperationReportRequestDTO opRequestDTO, UUID walletIdFilter) {
        String assetTypeName = opRequestDTO.getAssetType() != null ? opRequestDTO.getAssetType().name() : null;
        return withdrawRepository.findWithFilters(
                walletIdFilter,
                assetTypeName,
                opRequestDTO.getDateFrom(),
                opRequestDTO.getDateTo()
        );
    }

    @Override
    protected OperationReportResponseDTO map(WithdrawModel item, Map<UUID, ClientModel> walletToClient) {
        double quantity = item.getQuantity();
        double gross = item.getSellingPrice() * quantity;
        Double tax = item.getTax();
        Double net = item.getWithdrawValue();

        UUID walletId = walletIdOf(item);
        ClientModel client = walletToClient.get(walletId);
        LocalDateTime occuredAt = dateOf(item);

        return dtoMapperService.toOperationWithdrawReportDTO(
                item,
                client,
                quantity,
                gross,
                tax,
                net,
                occuredAt
        );
    }
}


