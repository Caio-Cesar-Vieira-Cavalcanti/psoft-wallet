package com.ufcg.psoft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcg.psoft.commerce.dto.client.AddressDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AddressModel;
import com.ufcg.psoft.commerce.model.user.ClientModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.ufcg.psoft.commerce.CommerceApplication;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CommerceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Client Controller Tests")
public class ClientControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    private static final String CLIENT_BASE_URL = "/clients";

    private UUID clientId;

    @BeforeEach
    void setup() {
        clientId = UUID.randomUUID();
        ClientModel client = new ClientModel(
                clientId,
                "João Azevedo",
                new EmailModel("joao@email.com"),
                new AccessCodeModel("123456"),
                new AddressModel("Street", "123", "Neighborhood", "City", "State", "Country", "12345-678"),
                PlanTypeEnum.PREMIUM,
                10000.0,
                null
        );

        clientId = clientRepository.save(client).getId();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
    }

    @Test
    @DisplayName("Create client successfully")
    void testCreateClient_Success() throws Exception {
        ClientPostRequestDTO dto = ClientPostRequestDTO.builder()
                .fullName("Maria Silva")
                .email("maria@email.com")
                .accessCode("654321")
                .budget(15000.0)
                .planType(PlanTypeEnum.NORMAL)
                .address(new AddressDTO("Street", "321", "Neighboorhood", "City", "State", "Country", "98765-432"))
                .build();

        mockMvc.perform(post(CLIENT_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Maria Silva"))
                .andExpect(jsonPath("$.budget").value(15000.0));
    }

    @Test
    @DisplayName("Get client by ID successfully")
    void testGetClientById_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL + "/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Azevedo"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("Delete client successfully")
    void testDeleteClient_Success() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patch client full name successfully")
    void testPatchClientFullName_Success() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "123456");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Carlos"));
    }

    @Test
    @DisplayName("Get all clients")
    void testGetAllClients_Success() throws Exception {
        mockMvc.perform(get(CLIENT_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fullName").value("João Azevedo"));
    }

    @Test
    @DisplayName("Invalid access code")
    void testPatchClientFullName_InvalidAcessCode() throws Exception {
        ClientPatchFullNameRequestDTO dto = new ClientPatchFullNameRequestDTO("João Carlos", "654321");

        mockMvc.perform(patch(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid access code")
    void testDeleteClient_InvalidAccessCode() throws Exception {
        ClientDeleteRequestDTO dto = new ClientDeleteRequestDTO("654321");

        mockMvc.perform(delete(CLIENT_BASE_URL + "/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid id")
    void testDeleteClient_WithInvalidId() throws Exception {
        UUID clientId = UUID.randomUUID();
        ClientDeleteRequestDTO requestDTO = new ClientDeleteRequestDTO("123456");

        mockMvc.perform(delete("/clients/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Invalid id")
    void testGetClientById_InvalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/clients/" + invalidId))
                .andExpect(status().isNotFound());
    }
}