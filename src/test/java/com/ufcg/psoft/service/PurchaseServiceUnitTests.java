package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.client.ClientPurchaseAssetRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPurchaseHistoryRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationRequestDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseAfterAddedInWalletDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.enums.PurchaseStateEnum;
import com.ufcg.psoft.commerce.exception.asset.AssetIsInactiveException;
import com.ufcg.psoft.commerce.exception.asset.AssetQuantityAvailableIsInsufficientException;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientBudgetIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.user.*;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.PurchaseModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.wallet.PurchaseRepository;
import com.ufcg.psoft.commerce.repository.wallet.WalletRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseService;
import com.ufcg.psoft.commerce.service.wallet.PurchaseServiceImpl;
import com.ufcg.psoft.commerce.service.wallet.WalletService;
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
    private WalletRepository walletRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private AdminService adminService;

    @Mock
    private ClientService clientService;

    @Mock
    private AssetService assetService;

    @Mock
    private WalletService walletService;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private DTOMapperService dtoMapperService;

    @InjectMocks
    private PurchaseServiceImpl purchaseServiceImpl;

    @InjectMocks
    private WalletServiceImpl walletServiceImpl;

    private UUID clientId;
    private UUID purchaseId;
    private UUID walletId;
    private UUID assetId;
    
    private ClientModel client;
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
        clientId = UUID.randomUUID();

        wallet = WalletModel.builder()
                .id(walletId)
                .budget(10000.0)
                .holdings(new HashMap<>())
                .build();

        client = new ClientModel(
                clientId,
                "Rafael Barreto",
                new EmailModel("rafael@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                wallet
        );
        when(clientService.validateClientAccess(clientId, "123456")).thenReturn(client);

        asset = AssetModel.builder()
                .id(assetId)
                .name("Test Stock")
                .description("Test stock asset")
                .quotation(100.0)
                .quotaQuantity(1000.0)
                .isActive(true)
                .build();
        when(assetService.fetchAsset(assetId)).thenReturn(asset);

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
        when(clientService.validateClientAccess(clientId, "123456")).thenReturn(client);
        when(assetService.fetchAsset(assetId)).thenReturn(asset);

        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 10);

        PurchaseResponseDTO expectedResponse = new PurchaseResponseDTO(
                purchase.getId(), wallet.getId(), asset.getId(),
                10.0, PurchaseStateEnum.REQUESTED, LocalDate.now()
        );
        when(dtoMapperService.toPurchaseResponseDTO(any(PurchaseModel.class))).thenReturn(expectedResponse);

        PurchaseResponseDTO result = purchaseServiceImpl.createPurchaseRequest(clientId, assetId, dto);

        verify(clientService).validateClientAccess(clientId, "123456");
        verify(assetService).fetchAsset(assetId);
        verify(purchaseRepository).save(any(PurchaseModel.class));
        verify(dtoMapperService).toPurchaseResponseDTO(any(PurchaseModel.class));

        assertNotNull(result);
        assertEquals(purchaseId, result.getId());
        assertEquals(wallet.getId(), result.getWalletId());
        assertEquals(asset.getId(), result.getAssetId());
        assertEquals(10, result.getQuantity());
        assertEquals(LocalDate.now(), result.getDate());
        assertEquals(PurchaseStateEnum.REQUESTED, result.getPurchaseState());

        verify(purchaseRepository).save(any(PurchaseModel.class));
        verify(dtoMapperService).toPurchaseResponseDTO(any(PurchaseModel.class));
    }

    @Test
    @DisplayName("Should get purchase history by wallet ID")
    void testShouldGetPurchaseHistoryByWalletId() {
        when(purchaseRepository.findByWalletId(walletId)).thenReturn(java.util.List.of(purchase));

        ClientPurchaseHistoryRequestDTO dto = new ClientPurchaseHistoryRequestDTO("123456", null, null, null);

        List<PurchaseResponseDTO> result = purchaseServiceImpl.getPurchaseHistory(clientId, dto);

        assertNotNull(result);
        assertEquals(1, result.size());

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
        PurchaseResponseDTO newPurchase = new PurchaseResponseDTO(UUID.randomUUID(), walletId, assetId, 2.0, PurchaseStateEnum.REQUESTED, LocalDate.now());

        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 2);

        when(purchaseService.createPurchaseRequest(clientId, assetId, dto)).thenReturn(newPurchase);

        PurchaseResponseDTO result = purchaseService.createPurchaseRequest(clientId, assetId, dto);

        assertInstanceOf(PurchaseResponseDTO.class, result);
        assertEquals(newPurchase, result);
        verify(purchaseService).createPurchaseRequest(clientId, assetId, dto);
    }

    @Test
    @DisplayName("Should throw exception when wallet budget is insufficient")
    void testRedirectCreatePurchaseRequest_InsufficientBudget() {
        wallet.setBudget(50.0);
        ClientPurchaseAssetRequestDTO dto = new ClientPurchaseAssetRequestDTO("123456", 999999);

        assertThrows(ClientBudgetIsInsufficientException.class,
                () -> purchaseServiceImpl.createPurchaseRequest(clientId, assetId, dto));
    }

    @Test
    @DisplayName("Should return empty list if no purchase history exists")
    void testRedirectGetPurchaseHistory_Empty() {
        ClientPurchaseHistoryRequestDTO dto = new ClientPurchaseHistoryRequestDTO("123456", null, null, null);

        when(purchaseServiceImpl.getPurchaseHistory(clientId, dto)).thenReturn(Collections.emptyList());

        List<PurchaseResponseDTO> result = purchaseServiceImpl.getPurchaseHistory(clientId, dto);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return null if holdings map is null")
    void testFindHoldingByAsset_NullHoldings() {
        wallet.setHoldings(null);

        HoldingModel result = walletServiceImpl.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return null if holdings are empty")
    void testFindHoldingByAsset_EmptyHoldings() {
        wallet.setHoldings(new HashMap<>());

        HoldingModel result = walletServiceImpl.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return holding if it matches the asset")
    void testFindHoldingByAsset_Found() {
        HoldingModel holding = HoldingModel.builder().asset(asset).build();
        wallet.getHoldings().put(asset.getId(), holding);

        HoldingModel result = walletServiceImpl.findHoldingByAsset(wallet, asset);

        assertEquals(holding, result);
    }

    @Test
    @DisplayName("Should return null if no holding matches the asset")
    void testFindHoldingByAsset_NotFound() {
        AssetModel otherAsset = AssetModel.builder().id(UUID.randomUUID()).build();
        HoldingModel holding = HoldingModel.builder().asset(otherAsset).build();
        wallet.getHoldings().put(otherAsset.getId(), holding);

        HoldingModel result = walletServiceImpl.findHoldingByAsset(wallet, asset);

        assertNull(result);
    }

    @Test
    @DisplayName("Should add purchase when holding already exists")
    void testAddPurchase_ExistingHolding() {
        when(adminService.getAdmin()).thenReturn(admin);
        HoldingModel holding = HoldingModel.builder().asset(asset).build();
        wallet.getHoldings().put(asset.getId(), holding);

        PurchaseModel newPurchase = PurchaseModel.builder()
                .asset(asset)
                .wallet(wallet)
                .build();

        newPurchase.modify(adminService.getAdmin());

        PurchaseResponseAfterAddedInWalletDTO dto =
                new PurchaseResponseAfterAddedInWalletDTO(newPurchase, holding);

        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(newPurchase);
        when(walletService.addedInWallet(newPurchase, holding)).thenReturn(dto);
        when(walletService.findHoldingByAsset(wallet, asset)).thenReturn(holding);

        PurchaseResponseDTO result = walletServiceImpl.addPurchase(newPurchase);

        assertEquals(PurchaseStateEnum.PURCHASED, result.getPurchaseState());
        assertEquals(dto.getQuantity(), result.getQuantity());
        assertEquals(dto.getDate(), result.getDate());
        assertEquals(dto.getWalletId(), result.getWalletId());
        assertEquals(dto.getAssetId(), result.getAssetId());
        verify(adminService).getAdmin();
    }

    @Test
    @DisplayName("Should confirm purchase by client successfully")
    void testConfirmPurchase() {
        when(adminService.getAdmin()).thenReturn(admin);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(PurchaseModel.class))).thenReturn(purchase);

        when(clientService.validateClientAccess(clientId, "123456")).thenReturn(client);

        HoldingModel holding = new HoldingModel(UUID.randomUUID(), asset, wallet, 2, 200);
        when(walletService.findHoldingByAsset(wallet, asset)).thenReturn(holding);

        purchase.modify(adminService.getAdmin());

        PurchaseResponseDTO expectedResponse = new PurchaseResponseDTO(
                purchase.getId(), wallet.getId(), asset.getId(),
                10.0, PurchaseStateEnum.PURCHASED, LocalDate.now() // ← Mude para PURCHASED
        );
        when(walletService.addPurchase(any(PurchaseModel.class))).thenReturn(expectedResponse);

        PurchaseConfirmationByClientDTO dto = new PurchaseConfirmationByClientDTO("123456");

        PurchaseResponseDTO result = purchaseServiceImpl.confirmPurchase(purchaseId, clientId, dto);

        assertNotNull(result);
        assertInstanceOf(PurchaseResponseDTO.class, result);
        assertEquals(PurchaseStateEnum.PURCHASED, result.getPurchaseState());

        verify(purchaseRepository).findById(purchaseId);
        verify(purchaseRepository).save(purchase);
        verify(walletService).addPurchase(any(PurchaseModel.class));
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when confirming by client with invalid ID")
    void testConfirmPurchase_NotFound() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        PurchaseConfirmationByClientDTO dto = new PurchaseConfirmationByClientDTO("654321");

        assertThrows(PurchaseNotFoundException.class, () -> {
            purchaseServiceImpl.confirmPurchase(purchaseId, clientId, dto);
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

        PurchaseResponseAfterAddedInWalletDTO result = walletServiceImpl.addedInWallet(purchase, holdingModel);

        assertNotNull(result);
        verify(holdingModel).increaseQuantityAfterPurchase(purchase.getQuantity());
        verify(holdingModel).increaseAccumulatedPrice(purchase.getQuantity() * purchase.getAcquisitionPrice());
        verify(purchaseRepository).save(purchase);
    }
}
