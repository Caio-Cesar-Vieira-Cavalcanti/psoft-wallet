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

    @GetMapping
    public ResponseEntity<List<AssetResponseDTO>> getAllAssets() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.getAllAssets());
    }

    @GetMapping("/{idAsset}")
    public ResponseEntity<AssetResponseDTO> getAssetById(@PathVariable UUID idAsset) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.getAssetById(idAsset));
    }

    @PatchMapping("/{idAsset}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable UUID idAsset,
                                                   @RequestBody @Valid AssetPatchRequestDTO assetPatchRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.update(idAsset, assetPatchRequestDTO));
    }

    @DeleteMapping("/{idAsset}")
    public ResponseEntity<?> delete(@PathVariable UUID idAsset,
                                    @RequestBody @Valid AssetDeleteRequestDTO assetDeleteRequestDTO) {
        assetService.delete(idAsset, assetDeleteRequestDTO);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @PatchMapping("/{idAsset}/quotation")
    public ResponseEntity<AssetResponseDTO> updateQuotation(@PathVariable UUID idAsset,
                                                            @RequestBody @Valid AssetQuotationUpdateDTO assetQuotationUpdateDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.updateQuotation(idAsset, assetQuotationUpdateDTO));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<AssetResponseDTO> setIsActive(
            @PathVariable("id") UUID idAsset,
            @RequestBody @Valid AssetStatusPatchDTO statusDTO
    ) {
        AssetResponseDTO responseDTO = assetService.setIsActive(idAsset, statusDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

}
