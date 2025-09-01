package com.ufcg.psoft.commerce.controller.withdraw;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.service.wallet.WithdrawService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/withdraws",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class WithdrawController {

    @Autowired
    WithdrawService withdrawService;

    @PostMapping("/{withdrawId}/confirmation")
    public ResponseEntity<WithdrawResponseDTO> confirmWithdraw(
            @PathVariable UUID withdrawId,
            @RequestBody @Valid WithdrawConfirmationRequestDTO withdrawConfirmationRequestDTO) {

        WithdrawResponseDTO updated = withdrawService.confirmWithdraw(withdrawId, withdrawConfirmationRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updated);
    }
} 
