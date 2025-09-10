package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientPurchaseAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;

import java.util.UUID;
import java.util.List;

public interface PurchaseService {
    List<PurchaseResponseDTO> getPurchaseHistory(UUID clientId, ClientPurchaseHistoryRequestDTO dto);

    PurchaseResponseDTO createPurchaseRequest(UUID clientId, UUID assetId, ClientPurchaseAssetRequestDTO dto);

    PurchaseResponseDTO confirmAvailability(UUID purchaseId, PurchaseConfirmationRequestDTO purchaseConfirmationRequestDTO);

    PurchaseResponseDTO confirmPurchase(UUID purchaseId, UUID clientId, PurchaseConfirmationByClientDTO dto);

}
