package com.ufcg.psoft.commerce.controller.asset;

import com.ufcg.psoft.commerce.dto.asset.AssetPostRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetPutRequestDTO;
import com.ufcg.psoft.commerce.dto.asset.AssetResponseDTO;

import com.ufcg.psoft.commerce.service.asset.AssetService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> getAssetById(@PathVariable UUID idAsset) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.getAssetById(idAsset));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable UUID idAsset,
                                    @RequestBody @Valid AssetPutRequestDTO assetPutRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetService.update(idAsset, assetPutRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID idAsset) {
        assetService.delete(idAsset);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

}
