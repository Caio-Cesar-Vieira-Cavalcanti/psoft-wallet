package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.wallet.WithdrawServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Withdraw Service Unit Tests")
public class WithdrawServiceUnitTests {

    private WithdrawServiceImpl withdrawService;
    private WalletRepository walletRepository;
    private HoldingRepository holdingRepository;
    private DTOMapperService dtoMapperService;

    private WalletModel wallet;
    private AssetModel asset;
    private HoldingModel holding;

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        holdingRepository = mock(HoldingRepository.class);
        dtoMapperService = mock(DTOMapperService.class);

        withdrawService = new WithdrawServiceImpl();
        ReflectionTestUtils.setField(withdrawService, "walletRepository", walletRepository);
        ReflectionTestUtils.setField(withdrawService, "holdingRepository", holdingRepository);
        ReflectionTestUtils.setField(withdrawService, "dtoMapperService", dtoMapperService);

        asset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Mock Asset")
                .quotation(50.0)
                .build();

        holding = HoldingModel.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .quantity(10.0)
                .accumulatedPrice(500.0)
                .build();

        wallet = WalletModel.builder()
                .id(UUID.randomUUID())
                .budget(1000.0)
                .holdings(new HashMap<>())
                .build();

        wallet.getHoldings().put(asset.getId(), holding);
    }

    @Test
    @DisplayName("Should withdraw asset successfully")
    void testWithdrawAsset_Success() {
        double quantityToWithdraw = 5.0;
        double expectedValue = quantityToWithdraw * asset.getQuotation();
        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, expectedValue))
                .thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertSame(mockResponse, result);
        assertEquals(5.0, holding.getQuantity());
        assertEquals(1000.0 + expectedValue, wallet.getBudget());
        verify(holdingRepository).save(holding);
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Should throw exception if holding does not exist")
    void testWithdrawAsset_HoldingNotFound() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        assertThrows(ClientHoldingIsInsufficientException.class, () ->
                withdrawService.withdrawAsset(wallet, otherAsset, 1.0)
        );
        verify(holdingRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception if quantity is insufficient")
    void testWithdrawAsset_InsufficientQuantity() {
        assertThrows(ClientHoldingIsInsufficientException.class, () ->
                withdrawService.withdrawAsset(wallet, asset, 20.0)
        );
        verify(holdingRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove holding if all quantity is withdrawn")
    void testWithdrawAsset_RemoveHolding() {
        double quantityToWithdraw = 10.0;
        double expectedValue = quantityToWithdraw * asset.getQuotation();
        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, expectedValue))
                .thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertSame(mockResponse, result);
        assertFalse(wallet.getHoldings().containsKey(holding.getId()));
        verify(holdingRepository).delete(holding);
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Should correctly update accumulatedPrice after withdrawal")
    void testWithdrawAsset_UpdateAccumulatedPrice() {
        double quantityToWithdraw = 2.0;
        double expectedPriceReduction = quantityToWithdraw * asset.getQuotation();

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, expectedPriceReduction))
                .thenReturn(mockResponse);

        withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertEquals(holding.getAccumulatedPrice(), 500.0 - expectedPriceReduction);
    }

    @Test
    @DisplayName("Should call dtoMapperService with correct arguments")
    void testWithdrawAsset_CallsDtoMapperService() {
        double quantityToWithdraw = 3.0;
        double withdrawValue = quantityToWithdraw * asset.getQuotation();

        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, withdrawValue))
                .thenReturn(mockResponse);

        withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        verify(dtoMapperService, times(1))
                .toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, withdrawValue);
    }

    @Test
    @DisplayName("Should not affect other holdings")
    void testWithdrawAsset_OtherHoldingsUnaffected() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).quotation(20.0).build();
        HoldingModel otherHolding = HoldingModel.builder().id(UUID.randomUUID()).asset(otherAsset).quantity(5.0).accumulatedPrice(100.0).build();
        wallet.getHoldings().put(otherAsset.getId(), otherHolding);

        double quantityToWithdraw = 5.0;
        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(wallet, asset, quantityToWithdraw, quantityToWithdraw * asset.getQuotation()))
                .thenReturn(mockResponse);

        withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertEquals(5.0, otherHolding.getQuantity());
        assertEquals(100.0, otherHolding.getAccumulatedPrice());
    }
}
