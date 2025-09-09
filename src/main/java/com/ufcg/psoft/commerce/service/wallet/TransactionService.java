package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.wallet.ExportTransactionsRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.ExportTransactionsResponseDTO;

import java.util.UUID;

public interface TransactionService {

    ExportTransactionsResponseDTO exportClientTransactionsCSV(UUID clientId, ExportTransactionsRequestDTO dto);

}
