package com.ufcg.psoft.commerce.controller.transaction;

import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsRequest;
import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsResponseDTO;
import com.ufcg.psoft.commerce.service.wallet.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/transactions",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @GetMapping("/export/{clientId}")
    public ResponseEntity<byte[]> exportClientTransactionsCSV(
            @PathVariable UUID clientId,
            @RequestBody @Valid ClientExportTransactionsRequest dto
    ) {
        ClientExportTransactionsResponseDTO responseDTO = transactionService.exportClientTransactionsCSV(clientId, dto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + responseDTO.getFileName())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(responseDTO.getContent());
    }

}
