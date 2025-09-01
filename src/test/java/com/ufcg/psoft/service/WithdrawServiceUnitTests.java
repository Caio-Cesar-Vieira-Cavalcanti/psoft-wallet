package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.HoldingRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.repository.wallet.WithdrawRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
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
    private WithdrawRepository withdrawRepository;
    private AdminService adminService;
    private DTOMapperService dtoMapperService;

    private WalletModel wallet;
    private AssetModel asset;
    private HoldingModel holding;

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        holdingRepository = mock(HoldingRepository.class);
        withdrawRepository = mock(WithdrawRepository.class);
        adminService = mock(AdminService.class);
        dtoMapperService = mock(DTOMapperService.class);

        withdrawService = new WithdrawServiceImpl();
        ReflectionTestUtils.setField(withdrawService, "walletRepository", walletRepository);
        ReflectionTestUtils.setField(withdrawService, "holdingRepository", holdingRepository);
        ReflectionTestUtils.setField(withdrawService, "withdrawRepository", withdrawRepository);
        ReflectionTestUtils.setField(withdrawService, "adminService", adminService);
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
        WithdrawResponseDTO mockResponse = mock(WithdrawResponseDTO.class);
        when(dtoMapperService.toWithdrawResponseDTO(any(), eq(0.0)))
                .thenReturn(mockResponse);

        WithdrawResponseDTO result = withdrawService.withdrawAsset(wallet, asset, quantityToWithdraw);

        assertSame(mockResponse, result);
        verify(withdrawRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception if holding does not exist")
    void testWithdrawAsset_HoldingNotFound() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        assertThrows(ClientHoldingIsInsufficientException.class, () ->
                withdrawService.withdrawAsset(wallet, otherAsset, 1.0)
        );
    }
}
