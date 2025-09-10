package com.ufcg.psoft.commerce.service.report.fetchers;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.enums.OperationTypeEnum;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PurchaseFetcher extends OperationFetcherTemplate<PurchaseModel> {
    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    DTOMapperService dtoMapperService;


    @Override
    protected boolean supportsOperationType(OperationTypeEnum operationTypeEnum) {
        return operationTypeEnum == null || operationTypeEnum == OperationTypeEnum.PURCHASE;
    }

    @Override
    protected List<PurchaseModel> findItems(OperationReportRequestDTO opRequestDTO, UUID walletIdFilter) {
        String assetTypeName = opRequestDTO.getAssetType() != null ? opRequestDTO.getAssetType().name() : null;
        return purchaseRepository.findWithFilters(
                walletIdFilter,
                assetTypeName,
                opRequestDTO.getDateFrom(),
                opRequestDTO.getDateTo()
        );
    }

    @Override
    protected OperationReportResponseDTO map(PurchaseModel item, Map<UUID, ClientModel> walletToClient) {
        double quantity = item.getQuantity();
        double unitPrice = item.getAcquisitionPrice();
        double gross = unitPrice * quantity;
        Double tax = null;
        Double net = null;

        UUID walletId = walletIdOf(item);
        ClientModel client = walletToClient.get(walletId);
        LocalDateTime occuredAt = dateOf(item);

        return dtoMapperService.toOperationPurchaseReportDTO(
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
