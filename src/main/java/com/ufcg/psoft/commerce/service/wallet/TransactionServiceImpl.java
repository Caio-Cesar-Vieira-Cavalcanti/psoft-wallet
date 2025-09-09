package com.ufcg.psoft.commerce.service.wallet;

import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsRequest;
import com.ufcg.psoft.commerce.dto.client.ClientExportTransactionsResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientWithdrawHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.TransactionExportDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionServiceImpl implements TransactionService {

    @Autowired
    PurchaseService purchaseService;

    @Autowired
    WithdrawService withdrawService;

    public ClientExportTransactionsResponseDTO exportClientTransactionsCSV(UUID clientId, ClientExportTransactionsRequest dto) {
        ClientPurchaseHistoryRequestDTO purchaseHistoryRequestDTO = new ClientPurchaseHistoryRequestDTO();
        purchaseHistoryRequestDTO.setAccessCode(dto.getAccessCode());
        List<PurchaseResponseDTO> purchases = purchaseService.getPurchaseHistory(clientId, purchaseHistoryRequestDTO);

        ClientWithdrawHistoryRequestDTO withdrawHistoryRequestDTO = new ClientWithdrawHistoryRequestDTO();
        withdrawHistoryRequestDTO.setAccessCode(dto.getAccessCode());
        List<WithdrawHistoryResponseDTO> withdraws = withdrawService.getWithdrawHistory(clientId, withdrawHistoryRequestDTO);

        List<TransactionExportDTO> transactions = new ArrayList<>(mapperPurchasesToTransactionExportDTO(purchases));
        transactions.addAll(mapperWithdrawsToTransactionExportDTO(withdraws));

        return buildCSV(transactions);
    }

    private List<TransactionExportDTO> mapperPurchasesToTransactionExportDTO(List<PurchaseResponseDTO> purchases) {
        List<TransactionExportDTO> transactions = new ArrayList<>();

        for (PurchaseResponseDTO p: purchases) {
            TransactionExportDTO t = new TransactionExportDTO();
            t.setType("PURCHASE");
            t.setAssetId(p.getAssetId());
            t.setQuantity(p.getQuantity());
            t.setTotalValue(p.getAcquisitionPrice() * p.getQuantity());
            t.setTax(0);
            t.setDate(p.getDate());
            t.setState(p.getPurchaseState().name());
            transactions.add(t);
        }

        return transactions;
    }

    private List<TransactionExportDTO> mapperWithdrawsToTransactionExportDTO(List<WithdrawHistoryResponseDTO> withdraws) {
        List<TransactionExportDTO> transactions = new ArrayList<>();

        for (WithdrawHistoryResponseDTO w: withdraws) {
            TransactionExportDTO t = new TransactionExportDTO();
            t.setType("WITHDRAW");
            t.setAssetId(w.getAssetId());
            t.setQuantity(w.getQuantityWithdrawn());
            t.setTotalValue(w.getTotalValue());
            t.setTax(w.getTax());
            t.setDate(w.getDate());
            t.setState(w.getState().name());
            transactions.add(t);
        }

        return transactions;
    }

    private ClientExportTransactionsResponseDTO buildCSV(List<TransactionExportDTO> transactions) {
//        StringBuilder csvBuilder = new StringBuilder();
//        csvBuilder.append("type,asset,quantity,total-value,tax,date,state\n");
//        for (TransactionExportDTO t : transactions) {
//            csvBuilder.append(t.getType()).append(",")
//                    .append(t.getAssetId()).append(",")
//                    .append(t.getQuantity()).append(",")
//                    .append(t.getTotalValue()).append(",")
//                    .append(t.getTax()).append(",")
//                    .append(t.getDate()).append(",")
//                    .append(t.getState()).append("\n");
//        }

        ClientExportTransactionsResponseDTO response = new ClientExportTransactionsResponseDTO();
//        response.setFileName("transactions.csv");
//        response.setContent(csvBuilder.toString().getBytes(StandardCharsets.UTF_8));

        return response;
    }

}
