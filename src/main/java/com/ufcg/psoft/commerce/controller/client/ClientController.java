package com.ufcg.psoft.commerce.controller.client;

import com.ufcg.psoft.commerce.dto.WalletResponseDTO;
import com.ufcg.psoft.commerce.dto.client.ClientDeleteRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPatchFullNameRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientPostRequestDTO;
import com.ufcg.psoft.commerce.dto.client.ClientResponseDTO;
import com.ufcg.psoft.commerce.service.client.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        value = "/clients",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ClientController {

    @Autowired
    ClientService clientService;

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable UUID id) {
        return this.clientService.getClientById(id);
    }

    @GetMapping()
    public ResponseEntity<List<ClientResponseDTO>> getClients() {
        return this.clientService.getClients();
    }

    @PostMapping()
    public ResponseEntity<ClientResponseDTO> create(@RequestBody @Valid ClientPostRequestDTO body) {
        return this.clientService.create(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> remove(@PathVariable UUID id, @RequestBody @Valid ClientDeleteRequestDTO body) {
        return this.clientService.remove(id, body);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> patchFullName(@PathVariable UUID id, @RequestBody @Valid ClientPatchFullNameRequestDTO body) {
        return this.clientService.patchFullName(id, body);
    }

    @GetMapping({"/id/purchases"})
    public ResponseEntity<WalletResponseDTO> getPurchaseHistory(@PathVariable UUID id) {
        return this.clientService.getPurchaseHistory(id);
    }

}
