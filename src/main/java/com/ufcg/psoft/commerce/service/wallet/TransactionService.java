package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsRequest;
import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsResponseDTO;

import java.util.UUID;

public interface TransactionService {

    ClientExportTransactionsResponseDTO exportClientTransactionsCSV(UUID clientId, ClientExportTransactionsRequest dto);

}
