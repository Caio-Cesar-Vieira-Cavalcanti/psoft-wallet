package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.client.ClientService;
import com.ufcg.psoft.commerce.service.client.ClientServiceImpl;
import com.ufcg.psoft.commerce.service.mapper.DTOMapperService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClientServiceUnitTests {

    private ClientRepository clientRepository;
    private ClientService clientService;
    private ModelMapper modelMapper;
    private DTOMapperService dtoMapperService;

    private UUID clientId;
    private ClientModel client;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);

        modelMapper = new ModelMapper();
        clientService = new ClientServiceImpl();
        dtoMapperService = new DTOMapperService(modelMapper);

        ReflectionTestUtils.setField(clientService, "clientRepository", clientRepository);
        ReflectionTestUtils.setField(clientService, "modelMapper", modelMapper);
        ReflectionTestUtils.setField(clientService, "dtoMapperService", dtoMapperService);

        clientId = UUID.randomUUID();
        client = new ClientModel(
            clientId,
            "João Azevedo",
            new EmailModel("joao@email.com"),
            new AccessCodeModel("123456"),
            new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
            PlanTypeEnum.PREMIUM,
            10000.0,
            null
        );
    }

    @Test
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
                .ignoringFields("accessCode", "id", "email", "wallet") // Id is generated, accessCode is not returned on DTO, email has a different structure, wallet is null
                .isEqualTo(client);
    }

    @Test
    void testCreateClient_WithAccessCodeHasLessThan6Digits() {
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
    void testPatchFullName_WithValidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("Rafael Barreto", "123456");

        ClientResponseDTO result = clientService.patchFullName(clientId, dto);

        assertEquals("Rafael Barreto", result.getFullName());
        verify(clientRepository).save(any());
    }

    @Test
    void testPatchFullName_WithInvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("Rafael Barreto", "654321");

        assertThrows(UnauthorizedUserAccessException.class, () ->
            clientService.patchFullName(clientId, dto));
    }

    @Test
    void testRemoveClient_WithValidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("123456");

        clientService.remove(clientId, dto);

        verify(clientRepository).delete(client);
        assertTrue(true);
    }

    @Test
    void testRemoveClient_WithInvalidAccessCode() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("000000");

        assertThrows(UnauthorizedUserAccessException.class, () ->
        clientService.remove(clientId, dto));

        verify(clientRepository, never()).delete(any());
    }

    @Test
    void testGetClientById_Success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.getClientById(clientId);

        assertNotNull(result);
        assertEquals(client.getFullName(), result.getFullName());
        assertEquals(client.getEmail().getEmail(), result.getEmail());
    }

    @Test
    void testGetClientById_InvalidId() {
        UUID invalidId = UUID.randomUUID();
        when(clientRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ClientIdNotFoundException.class, () -> clientService.getClientById(invalidId));
    }

    @Test
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
}