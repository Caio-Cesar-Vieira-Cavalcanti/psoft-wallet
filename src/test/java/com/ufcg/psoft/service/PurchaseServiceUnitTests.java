package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetQuantityAvailableIsInsufficientException;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientBudgetIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseServiceImpl;
import com.ufcg.psoft.commerce.service.wallet.WalletServiceImpl;
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
import java.util.*;

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

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private DTOMapperService dtoMapperService;

    @InjectMocks
    private PurchaseServiceImpl purchaseServiceImpl;

    @InjectMocks
    private WalletServiceImpl walletService;

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
                .holdings(new HashMap<>())
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


        dtoMapperService = mock(DTOMapperService.class);
    }

    @Test
    @DisplayName("Should confirm availability and change state from REQUESTED to AVAILABLE")
    void testShouldConfirmAvailabilityAndChangeStateFromRequestedToAvailable() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        PurchaseResponseDTO result = purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);

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
            purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);
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
            purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);
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
            purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);
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
            purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);
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

        PurchaseModel result = purchaseServiceImpl.createPurchaseRequest(wallet, asset, 100.0, 10);

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

        var result = purchaseServiceImpl.getPurchaseHistoryByWalletId(walletId);

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

        PurchaseResponseDTO result = purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);

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

        PurchaseResponseDTO result = purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);

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

        PurchaseResponseDTO result = purchaseServiceImpl.confirmAvailability(purchaseId, confirmationRequest);

        assertNotNull(result);
        assertEquals(PurchaseStateEnum.AVAILABLE, result.getPurchaseState());

        assertEquals(PurchaseStateEnum.AVAILABLE, purchase.getStateEnum());

        verify(purchaseRepository).findById(purchaseId);
        verify(adminService).getAdmin();
        verify(purchaseRepository).save(purchase);
        verify(admin).validateAccess(confirmationRequest.getAdminEmail(), confirmationRequest.getAdminAccessCode());
    }

    @Test
    @DisplayName("Should create purchase request when wallet has enough budget")
    void testRedirectCreatePurchaseRequest_Success() {
        PurchaseModel purchase = PurchaseModel.builder().id(UUID.randomUUID()).build();
        when(purchaseService.createPurchaseRequest(wallet, asset, 200.0, 2)).thenReturn(purchase);

        PurchaseModel result = walletService.redirectCreatePurchaseRequest(wallet, asset, 2);

        assertEquals(purchase, result);
        verify(purchaseService).createPurchaseRequest(wallet, asset, 200.0, 2);
    }

    @Test
    @DisplayName("Should throw exception when wallet budget is insufficient")
    void testRedirectCreatePurchaseRequest_InsufficientBudget() {
        wallet.setBudget(50.0);

        assertThrows(ClientBudgetIsInsufficientException.class,
                () -> walletService.redirectCreatePurchaseRequest(wallet, asset, 2));
    }

    @Test
    @DisplayName("Should return empty list if no purchase history exists")
    void testRedirectGetPurchaseHistory_Empty() {
        UUID walletId = wallet.getId();
        when(purchaseServiceImpl.getPurchaseHistoryByWalletId(walletId)).thenReturn(Collections.emptyList());

        List<PurchaseResponseDTO> result = walletService.redirectGetPurchaseHistory(walletId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return null if holdings map is null")
    void testFindHoldingByAsset_NullHoldings() {
        wallet.setHoldings(null);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null if holdings are empty")
    void testFindHoldingByAsset_EmptyHoldings() {
        wallet.setHoldings(new HashMap<>());

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return holding if it matches the asset")
    void testFindHoldingByAsset_Found() {
        HoldingModel holding = HoldingModel.builder().asset(asset).build();
        wallet.getHoldings().put(asset.getId(), holding);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertEquals(holding, result);
    }

    @Test
    @DisplayName("Should return null if no holding matches the asset")
    void testFindHoldingByAsset_NotFound() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        HoldingModel holding = HoldingModel.builder().asset(otherAsset).build();
        wallet.getHoldings().put(otherAsset.getId(), holding);

        HoldingModel result = walletService.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should add purchase when holding already exists")
    void testAddPurchase_ExistingHolding() {
        when(adminService.getAdmin()).thenReturn(admin);
        HoldingModel holding = HoldingModel.builder().asset(asset).build();
        wallet.getHoldings().put(asset.getId(), holding);

        PurchaseModel purchase = PurchaseModel.builder()
                .asset(asset)
                .wallet(wallet)
                .build();

        purchase.modify(adminService.getAdmin());

        PurchaseResponseDTO dto = new PurchaseResponseDTO();
        when(purchaseService.addedInWallet(purchase, holding)).thenReturn(dto);

        PurchaseResponseDTO result = walletService.addPurchase(purchase);

        assertSame(dto, result);
        verify(purchaseService).addedInWallet(purchase, holding);
        verify(adminService).getAdmin();
    }

    @Test
    @DisplayName("Should confirm purchase by client successfully")
    void testConfirmationByClient() {
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        purchase.modify(adminService.getAdmin());

        PurchaseModel result = purchaseServiceImpl.confirmationByClient(purchaseId);

        assertNotNull(result);
        assertEquals(purchase, result);
        assertEquals(PurchaseStateEnum.PURCHASED, result.getStateEnum());

        verify(purchaseRepository).findById(purchaseId);
        verify(purchaseRepository).save(purchase);
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when confirming by client with invalid ID")
    void testConfirmationByClient_NotFound() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> {
            purchaseServiceImpl.confirmationByClient(purchaseId);
        });

        verify(purchaseRepository).findById(purchaseId);
        verifyNoMoreInteractions(purchaseRepository);
    }


    @Test
    @DisplayName("Should add purchase to wallet when holding already exists")
    void testAddedInWallet_HoldingExists() {
        HoldingModel holdingModel = mock(HoldingModel.class);
        when(adminService.getAdmin()).thenReturn(admin);

        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        purchase.modify(adminService.getAdmin());

        PurchaseResponseDTO result = purchaseServiceImpl.addedInWallet(purchase, holdingModel);

        assertNotNull(result);
        verify(holdingModel).increaseQuantityAfterPurchase(purchase.getQuantity());
        verify(holdingModel).increaseAccumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice());
        verify(purchaseRepository).save(purchase);
    }
}
