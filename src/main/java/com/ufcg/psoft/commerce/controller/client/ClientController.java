package com.ufcg.psoft.commerce.controller.client;

import com.ufcg.psoft.commerce.dto.subscription.SubscriptionResponseDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseConfirmationByClientDTO;
import com.ufcg.psoft.commerce.dto.wallet.PurchaseResponseDTO;
import com.ufcg.psoft.commerce.dto.client.*;
import com.ufcg.psoft.commerce.dto.wallet.WalletHoldingResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawHistoryResponseDTO;
import com.ufcg.psoft.commerce.dto.wallet.WithdrawResponseDTO;
import com.ufcg.psoft.commerce.service.client.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

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

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable("clientId") UUID clientId) {
        ClientResponseDTO client = clientService.getClientById(clientId);
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

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> remove(@PathVariable("clientId") UUID clientId,
                                    @RequestBody @Valid ClientDeleteRequestDTO body) {

        clientService.remove(clientId, body);
        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> patchFullName(@PathVariable("clientId") UUID clientId,
                                                           @RequestBody @Valid ClientPatchFullNameRequestDTO body) {

        ClientResponseDTO updatedClient = clientService.patchFullName(clientId, body);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedClient);
    }

    @GetMapping("/{clientId}/assets")
    public ResponseEntity<List<AssetResponseDTO>> getActiveAssets(@PathVariable("clientId") UUID clientId,
                                                                  @RequestBody @Valid ClientActiveAssetsRequestDTO requestDTO) {
        List<AssetResponseDTO> activeAssets = clientService.redirectGetActiveAssets(clientId, requestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(activeAssets);
    }

    @GetMapping("/{clientId}/assets/{assetId}")
    public ResponseEntity<AssetResponseDTO> getAssetDetailsForClient(@PathVariable UUID clientId,
                                                                     @PathVariable UUID assetId,
                                                                     @RequestBody @Valid ClientAssetAccessRequestDTO dto) {
        AssetResponseDTO asset = clientService.redirectGetAssetDetails(clientId, assetId, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(asset);
    }

    @PatchMapping("/{clientId}/interest/price-variation")
    public ResponseEntity<SubscriptionResponseDTO> markInterestInPriceVariationOfAsset(@PathVariable("clientId") UUID clientId,
                                                                 @RequestBody @Valid ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO) {


        SubscriptionResponseDTO subscriptionResponseDTO = clientService.redirectMarkInterestInPriceVariationOfAsset(clientId, clientMarkInterestInAssetRequestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscriptionResponseDTO);
    }

    @PatchMapping("/{clientId}/interest/availability")
    public ResponseEntity<SubscriptionResponseDTO> markInterestInAvailabilityOfAsset(@PathVariable("clientId") UUID clientId,
                                                               @RequestBody @Valid ClientMarkInterestInAssetRequestDTO clientMarkInterestInAssetRequestDTO) {

        SubscriptionResponseDTO subscriptionResponseDTO = clientService.redirectMarkAvailabilityOfInterestInAsset(clientId, clientMarkInterestInAssetRequestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscriptionResponseDTO);
    }

    @GetMapping({"/{clientId}/wallet/purchase"})
    public ResponseEntity<List<PurchaseResponseDTO>> getPurchaseHistory(@PathVariable("clientId") UUID clientId,
                                                                @RequestBody @Valid ClientPurchaseHistoryRequestDTO clientPurchaseHistoryRequestDTO) {

        List<PurchaseResponseDTO> purchases = clientService.redirectGetPurchaseHistory(clientId, clientPurchaseHistoryRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(purchases);
    }

    @PostMapping("/{clientId}/wallet/purchase/{assetId}")
    public ResponseEntity<PurchaseResponseDTO> purchaseRequestForAvailableAsset(@PathVariable UUID clientId,
                                                                      @PathVariable UUID assetId,
                                                                      @RequestBody @Valid ClientPurchaseAssetRequestDTO dto) {

        PurchaseResponseDTO purchase = clientService.purchaseRequestForAvailableAsset(clientId, assetId, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(purchase);
    }

    @PostMapping("/{clientId}/wallet/purchase/{purchaseId}/confirmation-by-client")
    public ResponseEntity<PurchaseResponseDTO> confirmationByClient(
            @PathVariable UUID purchaseId,
            @PathVariable UUID clientId,
            @RequestBody @Valid PurchaseConfirmationByClientDTO dto
    ) {
        PurchaseResponseDTO updated = clientService.purchaseConfirmationByClient(purchaseId, clientId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{clientId}/wallet-holding")
    public ResponseEntity<WalletHoldingResponseDTO> getClientWalletHolding(
            @PathVariable UUID clientId,
            @RequestBody @Valid ClientWalletRequestDTO dto
    ) {
        WalletHoldingResponseDTO walletHolding = clientService.getClientWalletHolding(clientId, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(walletHolding);
    }
}
