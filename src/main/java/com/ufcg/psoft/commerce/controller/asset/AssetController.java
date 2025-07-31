package com.ufcg.psoft.commerce.controller.asset;

import com.ufcg.psoft.commerce.dto.asset.*;

import com.ufcg.psoft.commerce.service.asset.AssetService;
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
    value = "/assets",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class AssetController {

    @Autowired
    AssetService assetService;

    @PostMapping
    public ResponseEntity<AssetResponseDTO> create(@RequestBody @Valid AssetPostRequestDTO assetPostRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetService.create(assetPostRequestDTO));
    }

    @DeleteMapping("/{idAsset}")
    public ResponseEntity<?> delete(@PathVariable("idAsset") UUID idAsset,
                                    @RequestBody @Valid AssetDeleteRequestDTO assetDeleteRequestDTO) {
        assetService.delete(idAsset, assetDeleteRequestDTO);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @PatchMapping("/{idAsset}/quotation")
    public ResponseEntity<AssetResponseDTO> updateQuotation(@PathVariable("idAsset") UUID idAsset,
                                                            @RequestBody @Valid AssetQuotationUpdateDTO assetQuotationUpdateDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.updateQuotation(idAsset, assetQuotationUpdateDTO));
    }

    @PatchMapping("/{idAsset}/activation")
    public ResponseEntity<AssetResponseDTO> setIsActive(@PathVariable("idAsset") UUID idAsset,
                                                        @RequestBody @Valid AssetActivationPatchRequestDTO statusDTO) {
        AssetResponseDTO responseDTO = assetService.setIsActive(idAsset, statusDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AssetResponseDTO>> getAvailableAssets() {
        List<AssetResponseDTO> availableAssets = assetService.getAvailableAssets();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(availableAssets);
    }

}
