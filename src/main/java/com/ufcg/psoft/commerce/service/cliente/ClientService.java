package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ResponseEntity<ClientResponseDTO> getClientById(UUID id);

    ResponseEntity<List<ClientResponseDTO>> getClients();

    ResponseEntity<ClientResponseDTO> create(ClientPostRequestDTO body);

    ResponseEntity<ClientResponseDTO> remove(UUID id, ClientDeleteRequestDTO body);

    ResponseEntity<ClientResponseDTO> patchFullName(UUID id, ClientPatchFullNameRequestDTO body);
}
