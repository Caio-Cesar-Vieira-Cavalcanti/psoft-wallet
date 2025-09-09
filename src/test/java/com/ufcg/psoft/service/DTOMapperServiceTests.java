package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.enums.WithdrawStateEnum;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.model.wallet.WithdrawModel;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Mapper Service Tests")
class DTOMapperServiceTests {

    private DTOMapperService dtoMapperService;
    private ModelMapper modelMapper;

    private UUID walletId;
    private UUID assetId;
    private UUID withdrawId;
    private WalletModel wallet;
    private AssetModel asset;
    private WithdrawModel withdraw;

    @BeforeEach
    void setup() {
        modelMapper = new ModelMapper();
        dtoMapperService = new DTOMapperService(modelMapper);

        walletId = UUID.randomUUID();
        assetId = UUID.randomUUID();
        withdrawId = UUID.randomUUID();

        // Create asset type
        Stock stockType = new Stock();

        // Create asset
        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Asset")
                .assetType(stockType)
                .description("Test Asset Description")
                .isActive(true)
                .quotation(100.0)
                .quotaQuantity(1.0)
                .build();

        // Create wallet
        wallet = WalletModel.builder()
                .id(walletId)
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        // Create withdraw
        withdraw = WithdrawModel.builder()
                .id(withdrawId)
                .asset(asset)
                .wallet(wallet)
                .quantity(10.0)
                .date(LocalDate.now())
                .sellingPrice(100.0)
                .tax(10.0)
                .withdrawValue(990.0)
                .stateEnum(WithdrawStateEnum.REQUESTED)
                .build();
    }

    @Test
    @DisplayName("Should map withdraw model to response DTO with withdrawId")
    void testToWithdrawResponseDTO_WithWithdrawModel() {
        double valueReceived = 990.0;

        WithdrawResponseDTO result = dtoMapperService.toWithdrawResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(withdrawId, result.getWithdrawId());
        assertEquals(walletId, result.getWalletId());
        assertEquals(assetId, result.getAssetId());
        assertEquals(10.0, result.getQuantityWithdrawn());
        assertEquals(valueReceived, result.getValueReceived());
        assertEquals(1000.0, result.getNewWalletBudget());
        assertEquals(WithdrawStateEnum.REQUESTED, result.getState());
    }

    @Test
    @DisplayName("Should map withdraw model to response DTO with zero value received")
    void testToWithdrawResponseDTO_WithZeroValueReceived() {
        withdraw.setWithdrawValue(0.0);

        WithdrawResponseDTO result = dtoMapperService.toWithdrawResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(withdrawId, result.getWithdrawId());
        assertEquals(0.0, result.getValueReceived());
        assertEquals(WithdrawStateEnum.REQUESTED, result.getState());
    }

    @Test
    @DisplayName("Should map withdraw model with different state")
    void testToWithdrawResponseDTO_WithDifferentState() {
        withdraw.setStateEnum(WithdrawStateEnum.CONFIRMED);

        WithdrawResponseDTO result = dtoMapperService.toWithdrawResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(WithdrawStateEnum.CONFIRMED, result.getState());
    }

    @Test
    @DisplayName("Should map withdraw model with IN_ACCOUNT state")
    void testToWithdrawResponseDTO_WithInAccountState() {
        withdraw.setStateEnum(WithdrawStateEnum.IN_ACCOUNT);

        WithdrawResponseDTO result = dtoMapperService.toWithdrawResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(WithdrawStateEnum.IN_ACCOUNT, result.getState());
    }

    @Test
    @DisplayName("Should handle null withdraw model gracefully")
    void testToWithdrawResponseDTO_WithNullWithdrawModel() {
        assertThrows(NullPointerException.class, () -> {
            dtoMapperService.toWithdrawResponseDTO(null);
        });
    }

    @Test
    @DisplayName("Should map withdraw model with updated wallet budget")
    void testToWithdrawResponseDTO_WithUpdatedWalletBudget() {
        wallet.setBudget(2000.0);

        WithdrawResponseDTO result = dtoMapperService.toWithdrawResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(2000.0, result.getNewWalletBudget());
    }

    @Test
    @DisplayName("Should map withdraw model to history response DTO with correct values")
    void testToWithdrawHistoryResponseDTO_WithCorrectValues() {
        WithdrawHistoryResponseDTO result = dtoMapperService.toWithdrawHistoryResponseDTO(withdraw);

        assertNotNull(result);
        assertEquals(withdrawId, result.getWithdrawId());
        assertEquals("Test Asset", result.getAssetName());
        assertEquals(assetId, result.getAssetId());
        assertEquals(10.0, result.getQuantityWithdrawn());
        assertEquals(100.0, result.getSellingPrice());
        assertEquals(990.0, result.getTotalValue());
        assertEquals(10.0, result.getTax());
        assertEquals(LocalDate.now(), result.getDate());
        assertEquals(WithdrawStateEnum.REQUESTED, result.getState());
    }
}
