package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.HoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WalletHoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.asset.AssetType;
import com.ufcg.psoft.commerce.model.asset.types.Crypto;
import com.ufcg.psoft.commerce.model.asset.types.Stock;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.asset.AssetService;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.client.ClientServiceImpl;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;

import com.ufcg.psoft.commerce.service.wallet.WithdrawServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Client Service Unit Tests")
class ClientServiceUnitTests {

    private ClientRepository clientRepository;
    private ClientService clientService;
    private ModelMapper modelMapper;
    private DTOMapperService dtoMapperService;

    private UUID clientId;
    private ClientModel client;

    private AssetService assetService;
    private UUID assetId;
    private AssetResponseDTO assetResponseDTO;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);

        modelMapper = new ModelMapper();
        clientService = new ClientServiceImpl();
        dtoMapperService = new DTOMapperService(modelMapper);

        ReflectionTestUtils.setField(clientService, "clientRepository", clientRepository);
        ReflectionTestUtils.setField(clientService, "modelMapper", modelMapper);
        ReflectionTestUtils.setField(clientService, "dtoMapperService", dtoMapperService);

        WalletModel wallet = WalletModel.builder()
                .budget(5000)
                .holdings(new HashMap<>())
                .build();

        clientId = UUID.randomUUID();
        client = new ClientModel(
            clientId,
            "João Azevedo",
            new EmailModel("joao@email.com"),
            new AccessCodeModel("123456"),
            new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
            PlanTypeEnum.PREMIUM,
            wallet
        );

        assetService = mock(AssetService.class);
        ReflectionTestUtils.setField(clientService, "assetService", assetService);

        assetId = UUID.randomUUID();
        assetResponseDTO = AssetResponseDTO.builder()
                .id(assetId)
                .name("Bitcoin")
                .description("Best crypto ever")
                .quotation(100000.0)
                .quotaQuantity(20.0)
                .build();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create client successfully")
    void testCreateClient_Success() {
        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .fullName("João Azevedo")
                .email("joao@email.com")
                .accessCode("123456")
                .budget(10000.0)
                .planType(PlanTypeEnum.PREMIUM)
                .address(new AddressDTO("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .build();

        when(clientRepository.save(any(ClientModel.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientResponseDTO result = clientService.create(dto);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("accessCode", "id", "email", "wallet", "budget") // Id is generated, accessCode is not returned on DTO, email has a different structure, wallet is null
                .isEqualTo(client);
    }

    @Test
    @DisplayName("Should throw exception because the access code has less than 6 digits")
    void testCreateClient_WithAccessCodeLessThan6Digits() {
        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .fullName("João Azevedo")
                .email("joao@email.com")
                .accessCode("123")
                .budget(10000.0)
                .planType(PlanTypeEnum.PREMIUM)
                .address(new AddressDTO("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .build();

        when(clientRepository.save(any(ClientModel.class))).thenAnswer(inv -> inv.getArgument(0));
    
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        clientService.create(dto);
        });
        assertEquals("The access code must contain exactly 6 digits.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception because the email is invalid")
    void testCreateClient_WithNoNameAndEmail() {
        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .accessCode("123456")
                .budget(10000.0)
                .planType(PlanTypeEnum.PREMIUM)
                .address(new AddressDTO("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .build();

        when(clientRepository.save(any(ClientModel.class))).thenAnswer(inv -> inv.getArgument(0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.create(dto);
        });
        assertEquals("Invalid email", exception.getMessage());
    }

    @Test
    @DisplayName("Should patch a existing client full name successfully")
    void testPatchFullName_WithValidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("Rafael Barreto", "123456");

        ClientResponseDTO result = clientService.patchFullName(clientId, dto);

        assertEquals("Rafael Barreto", result.getFullName());
        verify(clientRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception because the client's access code is invalid")
    void testPatchFullName_WithInvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("Rafael Barreto", "654321");

        assertThrows(UnauthorizedUserAccessException.class, () ->
            clientService.patchFullName(clientId, dto));
    }

    @Test
    @DisplayName("Should remove the existing client successfully")
    void testRemoveClient_WithValidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("123456");

        clientService.remove(clientId, dto);

        verify(clientRepository).delete(client);
        assertTrue(true);
    }

    @Test
    @DisplayName("Should throw exception because the client's access code is invalid")
    void testRemoveClient_WithInvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("000000");

        assertThrows(UnauthorizedUserAccessException.class, () ->
        clientService.remove(clientId, dto));

        verify(clientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get the client by id successfully")
    void testGetClientById_Success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.getClientById(clientId);

        assertNotNull(result);
        assertEquals(client.getFullName(), result.getFullName());
        assertEquals(client.getEmail().getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception because the client doesn't exist")
    void testGetClientById_InvalidId() {
        UUID invalidId = UUID.randomUUID();
        when(clientRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ClientIdNotFoundException.class, () -> clientService.getClientById(invalidId));
    }

    @Test
    @DisplayName("Should get all existing clients successfully")
    void testGetClients_Success() {
        when(clientRepository.findAll()).thenReturn(List.of(client, client));

        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .fullName("Rafael Barreto")
                .email("Rafael@email.com")
                .accessCode("654321")
                .budget(10000.0)
                .planType(PlanTypeEnum.PREMIUM)
                .address(new AddressDTO("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"))
                .build();

        when(clientRepository.save(any(ClientModel.class))).thenAnswer(inv -> inv.getArgument(0));

        clientService.create(dto);

        List<ClientResponseDTO> result = clientService.getClients();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João Azevedo", result.get(0).getFullName());
    }

    @Test
    @DisplayName("Should return asset details with valid accessCode")
    void testGetAssetDetails_WithValidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(assetService.getAssetById(assetId)).thenReturn(assetResponseDTO);

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("123456")
                .build();

        AssetResponseDTO result = clientService.redirectGetAssetDetails(clientId, assetId, dto);

        assertNotNull(result);
        assertEquals("Bitcoin", result.getName());
        assertEquals("Best crypto ever", result.getDescription());
        assertEquals(100000.0, result.getQuotation());
        assertEquals(20.0, result.getQuotaQuantity());
        verify(clientRepository).findById(clientId);
        verify(assetService).getAssetById(assetId);
    }

    @Test
    @DisplayName("Should return asset details with valid accessCode")
    void testGetAssetDetails_WithInvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("000000")
                .build();

        assertThrows(UnauthorizedUserAccessException.class, () -> {
            clientService.redirectGetAssetDetails(clientId, assetId, dto);
        });

        verify(clientRepository).findById(clientId);
        verify(assetService, never()).getAssetById(any());
    }

    @Test
    @DisplayName("Should not return asset details with invalid accessCode")
    void testGetAssetDetails_ClientNotFound() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ClientAssetAccessRequestDTO dto = ClientAssetAccessRequestDTO.builder()
                .accessCode("123456")
                .build();

        assertThrows(ClientIdNotFoundException.class, () -> {
            clientService.redirectGetAssetDetails(clientId, assetId, dto);
        });

        verify(clientRepository).findById(clientId);
        verify(assetService, never()).getAssetById(any());
    }

    @Test
    @DisplayName("Should get client wallet holding and calculate values correctly")
    void testGetClientWalletHolding_Success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        createAndAddHoldingToClient("Bitcoin", new Crypto(), "CRYPTO", 100000.0, 0.5, 45000.0);
        createAndAddHoldingToClient("Tesla Stock", new Stock(), "STOCK", 200.0, 10.0, 1900.0);

        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("123456")
                .build();

        WalletHoldingResponseDTO result = clientService.getClientWalletHolding(clientId, dto);

        assertNotNull(result);
        assertEquals(client.getWallet().getId(), result.getWalletResponseDTO().getId());
        assertEquals(client.getWallet().getBudget(), result.getWalletResponseDTO().getBudget());
        assertEquals(2, result.getHoldings().size());

        assertEquals(46900.0, result.getTotalInvested()); // 45000.0 + 1900.0
        assertEquals(52000.0, result.getTotalCurrent()); // (0.5 * 100000) + (10 * 200)
        assertEquals(5100.0, result.getTotalPerformance()); // 52000.0 - 46900.0
    }

    @Test
    @DisplayName("Should return empty holdings when wallet holdings is null")
    void testGetClientWalletHolding_NullHoldings() {
        WalletModel walletWithNullHoldings = WalletModel.builder()
                .budget(client.getWallet().getBudget())
                .holdings(null)
                .build();

        ClientModel clientWithNullHoldings = new ClientModel(
                clientId,
                client.getFullName(),
                client.getEmail(),
                client.getAccessCode(),
                client.getAddress(),
                client.getPlanType(),
                walletWithNullHoldings
        );

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientWithNullHoldings));

        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("123456")
                .build();

        WalletHoldingResponseDTO result = clientService.getClientWalletHolding(clientId, dto);

        assertNotNull(result);
        assertTrue(result.getHoldings().isEmpty());
    }

    @Test
    @DisplayName("Should calculate acquisition price as zero when quantity is zero")
    void testMapHoldingToDTO_ZeroQuantity() {
        createAndAddHoldingToClient("AAPL", new Stock(), "STOCK", 150.0, 0.0, 1000.0);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientWalletRequestDTO dto = ClientWalletRequestDTO.builder()
                .accessCode("123456")
                .build();

        WalletHoldingResponseDTO result = clientService.getClientWalletHolding(clientId, dto);

        assertNotNull(result);
        assertEquals(1, result.getHoldings().size());

        HoldingResponseDTO responseHolding = result.getHoldings().get(0);
        assertEquals(0.0, responseHolding.getQuantity());
        assertEquals(0.0, responseHolding.getAcquisitionPrice(), "Acquisition price should be 0.0 when quantity is 0");
    }

    @Test
    @DisplayName("Should withdraw client asset successfully")
    void testWithdrawClientAsset_Success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        AssetModel asset = AssetModel.builder().id(assetId).name("Bitcoin").quotation(100000.0).build();
        when(assetService.getAssetById(assetId)).thenReturn(assetResponseDTO);
        
        ReflectionTestUtils.setField(clientService, "assetRepository", mock(com.ufcg.psoft.commerce.repository.asset.AssetRepository.class));
        com.ufcg.psoft.commerce.repository.asset.AssetRepository assetRepository =
                (com.ufcg.psoft.commerce.repository.asset.AssetRepository) ReflectionTestUtils.getField(clientService, "assetRepository");
        assertNotNull(assetRepository);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        WithdrawResponseDTO mockWithdrawResponse = mock(WithdrawResponseDTO.class);
        WithdrawServiceImpl withdrawService = mock(WithdrawServiceImpl.class);
        ReflectionTestUtils.setField(clientService, "withdrawService", withdrawService);
        when(withdrawService.withdrawAsset(client.getWallet(), asset, 5.0)).thenReturn(mockWithdrawResponse);

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        WithdrawResponseDTO result = clientService.withdrawClientAsset(clientId, assetId, dto);

        assertSame(mockWithdrawResponse, result);
        verify(clientRepository).findById(clientId);
        verify(withdrawService).withdrawAsset(client.getWallet(), asset, 5.0);
    }

    @Test
    @DisplayName("Should throw exception if client not found")
    void testWithdrawClientAsset_ClientNotFound() {
        UUID invalidClientId = UUID.randomUUID();
        when(clientRepository.findById(invalidClientId)).thenReturn(Optional.empty());

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(ClientIdNotFoundException.class, () ->
                clientService.withdrawClientAsset(invalidClientId, assetId, dto)
        );
    }

    @Test
    @DisplayName("Should throw exception if access code is invalid")
    void testWithdrawClientAsset_InvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("000000")
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(UnauthorizedUserAccessException.class, () ->
                clientService.withdrawClientAsset(clientId, assetId, dto)
        );
    }

    @Test
    @DisplayName("Should throw exception if asset not found")
    void testWithdrawClientAsset_AssetNotFound() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ReflectionTestUtils.setField(clientService, "assetRepository", mock(com.ufcg.psoft.commerce.repository.asset.AssetRepository.class));
        com.ufcg.psoft.commerce.repository.asset.AssetRepository assetRepository =
                (com.ufcg.psoft.commerce.repository.asset.AssetRepository) ReflectionTestUtils.getField(clientService, "assetRepository");
        assertNotNull(assetRepository);
        when(assetRepository.findById(assetId)).thenReturn(Optional.empty());

        ClientWithdrawAssetRequestDTO dto = ClientWithdrawAssetRequestDTO.builder()
                .accessCode("123456")
                .quantityToWithdraw(5.0)
                .build();

        assertThrows(com.ufcg.psoft.commerce.exception.asset.AssetNotFoundException.class, () ->
                clientService.withdrawClientAsset(clientId, assetId, dto)
        );
    }

    private void createAndAddHoldingToClient(String assetName, AssetType type, String typeName, double quotation, double quantity, double accumulatedPrice) {
        type.setId(1L);
        type.setName(typeName);

        AssetModel newAsset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name(assetName)
                .quotation(quotation)
                .assetType(type)
                .build();

        HoldingModel newHolding = HoldingModel.builder()
                .asset(newAsset)
                .quantity(quantity)
                .accumulatedPrice(accumulatedPrice)
                .wallet(client.getWallet())
                .build();

        client.getWallet().getHoldings().put(newAsset.getId(), newHolding);
    }
}