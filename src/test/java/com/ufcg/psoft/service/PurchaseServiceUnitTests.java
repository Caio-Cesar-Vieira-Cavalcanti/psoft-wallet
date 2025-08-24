package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetQuantityAvailableIsInsufficientException;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Purchase Service Unit Tests")
class PurchaseServiceUnitTests {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private PurchaseService purchaseService = new PurchaseServiceImpl();

    private UUID purchaseId;
    private UUID walletId;
    private UUID assetId;
    private PurchaseModel purchase;
    private WalletModel wallet;
    private AssetModel asset;
    private AdminModel admin;
    private PurchaseConfirmationRequestDTO confirmationRequest;

    @BeforeEach
    void setup() {
        purchaseId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        assetId = UUID.randomUUID();

        wallet = WalletModel.builder()
                .id(walletId)
                .budget(10000.0)
                .build();

        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Stock")
                .description("Test stock asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .build();

        admin = mock(AdminModel.class);
        when(admin.isAdmin()).thenReturn(true);

        purchase = PurchaseModel.builder()
                .id(purchaseId)
                .wallet(wallet)
                .asset(asset)
                .quantity(10)
                .acquisitionPrice(100.0)
                .date(LocalDate.now())
                .stateEnum(PurchaseStateEnum.REQUESTED)
                .build();
        
        purchase.loadState();

        confirmationRequest = PurchaseConfirmationRequestDTO.builder()
                .adminEmail("admin@example.com")
                .adminAccessCode("123456")
                .build();
    }

    @Test
    @DisplayName("Should confirm availability and change state from REQUESTED to AVAILABLE")
    void testShouldConfirmAvailabilityAndChangeStateFromRequestedToAvailable() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseResponseDTO result = purchaseService.confirmAvailability(purchaseId, confirmationRequest);

        assertNotNull(result);
        assertEquals(purchaseId, result.getId());
        assertEquals(walletId, result.getWalletId());
        assertEquals(assetId, result.getAssetId());
        assertEquals(10.0, result.getQuantity());
        assertEquals(PurchaseStateEnum.AVAILABLE, result.getPurchaseState());
        assertEquals(LocalDate.now(), result.getDate());

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(purchaseRepository).save(purchase);
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when purchase not found")
    void testShouldThrowPurchaseNotFoundExceptionWhenPurchaseNotFound() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> {
            purchaseService.confirmAvailability(purchaseId, confirmationRequest);
        });

        verify(purchaseRepository).findById(purchaseId);
        verifyNoMoreInteractions(purchaseRepository, adminService);
    }

    @Test
    @DisplayName("Should throw UnauthorizedUserAccessException when admin validation fails")
    void testShouldThrowUnauthorizedUserAccessExceptionWhenAdminValidationFails() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        doThrow(new UnauthorizedUserAccessException("Invalid credentials"))
                .when(admin).validateAccess(anyString(), anyString());

        assertThrows(UnauthorizedUserAccessException.class, () -> {
            purchaseService.confirmAvailability(purchaseId, confirmationRequest);
        });

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("Should throw AssetIsInactiveException when asset is inactive")
    void testShouldThrowAssetIsInactiveExceptionWhenAssetIsInactive() {
        asset.setActive(false);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);

        assertThrows(AssetIsInactiveException.class, () -> {
            purchaseService.confirmAvailability(purchaseId, confirmationRequest);
        });

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("Should throw AssetQuantityAvailableIsInsufficientException when insufficient quantity")
    void testShouldThrowAssetQuantityAvailableIsInsufficientExceptionWhenInsufficientQuantity() {
        asset.setQuotaQuantity(5.0); // Less than purchase quantity (10)
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);

        assertThrows(AssetQuantityAvailableIsInsufficientException.class, () -> {
            purchaseService.confirmAvailability(purchaseId, confirmationRequest);
        });

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
        verifyNoMoreInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("Should create purchase request successfully")
    void testShouldCreatePurchaseRequestSuccessfully() {
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseModel result = purchaseService.createPurchaseRequest(wallet, asset, 100.0, 10);

        assertNotNull(result);
        assertEquals(purchaseId, result.getId());
        assertEquals(wallet, result.getWallet());
        assertEquals(asset, result.getAsset());
        assertEquals(10, result.getQuantity());
        assertEquals(100.0, result.getAcquisitionPrice());
        assertEquals(LocalDate.now(), result.getDate());
        assertEquals(PurchaseStateEnum.REQUESTED, result.getStateEnum());

        verify(purchaseRepository).save(any(PurchaseModel.class));
    }

    @Test
    @DisplayName("Should get purchase history by wallet ID")
    void testShouldGetPurchaseHistoryByWalletId() {
        when(purchaseRepository.findByWalletId(walletId)).thenReturn(java.util.List.of(purchase));

        var result = purchaseService.getPurchaseHistoryByWalletId(walletId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(purchase, result.get(0));

        verify(purchaseRepository).findByWalletId(walletId);
    }

    @Test
    @DisplayName("Should verify asset availability before confirming purchase")
    void testShouldVerifyAssetAvailabilityBeforeConfirmingPurchase() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseResponseDTO result = purchaseService.confirmAvailability(purchaseId, confirmationRequest);

        assertNotNull(result);
        assertEquals(PurchaseStateEnum.AVAILABLE, result.getPurchaseState());
        
        assertTrue(asset.isActive());
        assertTrue(asset.getQuotaQuantity() >= purchase.getQuantity());

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(purchaseRepository).save(purchase);
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
    }

    @Test
    @DisplayName("Should verify liquidity sufficiency before confirming purchase")
    void testShouldVerifyLiquiditySufficiencyBeforeConfirmingPurchase() {
        asset.setQuotaQuantity(10.0); // Exactly the same as purchase quantity
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseResponseDTO result = purchaseService.confirmAvailability(purchaseId, confirmationRequest);

        assertNotNull(result);
        assertEquals(PurchaseStateEnum.AVAILABLE, result.getPurchaseState());

        assertEquals(10.0, asset.getQuotaQuantity());
        assertEquals(10.0, purchase.getQuantity());

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(purchaseRepository).save(purchase);
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
    }

    @Test
    @DisplayName("Should change state from REQUESTED to AVAILABLE when all conditions are met")
    void testShouldChangeStateFromRequestedToAvailableWhenAllConditionsAreMet() {
        purchase.setStateEnum(PurchaseStateEnum.REQUESTED);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseResponseDTO result = purchaseService.confirmAvailability(purchaseId, confirmationRequest);

        assertNotNull(result);
        assertEquals(PurchaseStateEnum.AVAILABLE, result.getPurchaseState());
        
        assertEquals(PurchaseStateEnum.AVAILABLE, purchase.getStateEnum());

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(purchaseRepository).save(purchase);
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
    }
}
