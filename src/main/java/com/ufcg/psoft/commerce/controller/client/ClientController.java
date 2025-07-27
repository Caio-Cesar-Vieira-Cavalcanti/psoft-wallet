package com.ufcg.psoft.commerce.controller.client;

import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletResponseDTO;
import com.ufcg.psoft.commerce.service.client.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable("id") UUID id) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(client);
    }

    @GetMapping()
    public ResponseEntity<List<ClientResponseDTO>> getClients() {
        List<ClientResponseDTO> clients = clientService.getClients();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clients);
    }

    @PostMapping()
    public ResponseEntity<ClientResponseDTO> create(@RequestBody @Valid ClientPostRequestDTO body) {
        ClientResponseDTO createdClient = clientService.create(body);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable("id") UUID id,
                                    @RequestBody @Valid ClientDeleteRequestDTO body) {

        clientService.remove(id, body);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> patchFullName(@PathVariable("id") UUID id,
                                                           @RequestBody @Valid ClientPatchFullNameRequestDTO body) {

        ClientResponseDTO updatedClient = clientService.patchFullName(id, body);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedClient);
    }

    @GetMapping({"/{id}/purchases"})
    public ResponseEntity<WalletResponseDTO> getPurchaseHistory(@PathVariable("id") UUID id,
                                                                @RequestBody @Valid ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {

        WalletResponseDTO purchases = clientService.getPurchaseHistory(id, clientPurchaseHistoryRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(purchases);
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<AssetResponseDTO>> getActiveAssets(@PathVariable UUID id,
                                                                  @RequestBody @Valid ClientActiveAssetsRequestDTO requestDTO) {
        List<AssetResponseDTO> activeAssets = clientService.redirectGetActiveAssets(id, requestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(activeAssets);
    }

}
