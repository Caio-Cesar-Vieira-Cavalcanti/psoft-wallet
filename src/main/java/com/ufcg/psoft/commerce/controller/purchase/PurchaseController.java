package com.ufcg.psoft.commerce.controller.purchase;

import com.ufcg.psoft.commerce.dto.client.ClientPurchaseAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        value = "/purchases",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class PurchaseController {

    @Autowired
    PurchaseService purchaseService;

    @GetMapping({"/{clientId}/wallet/purchase"})
    public ResponseEntity<List<PurchaseResponseDTO>> getPurchaseHistory(@PathVariable("clientId") UUID clientId,
                                                                        @RequestBody @Valid ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {

        List<PurchaseResponseDTO> purchases = purchaseService.getPurchaseHistory(clientId, clientPurchaseHistoryRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(purchases);
    }

    @PostMapping("/{purchaseId}/availability-confirmation")
    public ResponseEntity<PurchaseResponseDTO> confirmAvailability(
            @PathVariable UUID purchaseId,
            @RequestBody @Valid PurchaseConfirmationRequestDTO purchaseConfirmationRequestDTO) {

        PurchaseResponseDTO updated = purchaseService.confirmAvailability(purchaseId, purchaseConfirmationRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updated);
    }

    @PostMapping("/{clientId}/wallet/purchase/{purchaseId}/confirmation-by-client")
    public ResponseEntity<PurchaseResponseDTO> confirmPurchase(
            @PathVariable UUID purchaseId,
            @PathVariable UUID clientId,
            @RequestBody @Valid PurchaseConfirmationByClientDTO dto
    ) {
        PurchaseResponseDTO updated = purchaseService.confirmPurchase(purchaseId, clientId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{clientId}/wallet/purchase/{assetId}")
    public ResponseEntity<PurchaseResponseDTO> purchaseRequestForAvailableAsset(@PathVariable UUID clientId,
                                                                                @PathVariable UUID assetId,
                                                                                @RequestBody @Valid ClientPurchaseAssetRequestDTO dto) {

        PurchaseResponseDTO purchase = purchaseService.createPurchaseRequest(clientId, assetId, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(purchase);
    }
}

