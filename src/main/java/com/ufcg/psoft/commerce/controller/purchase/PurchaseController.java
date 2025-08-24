package com.ufcg.psoft.commerce.controller.purchase;

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

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/purchases",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class PurchaseController {

    @Autowired
    PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
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
}

