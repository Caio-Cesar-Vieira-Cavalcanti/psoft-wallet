package com.ufcg.psoft.commerce.controller.asset;

import com.ufcg.psoft.commerce.dto.asset.AssetTypeResponseDTO;
import com.ufcg.psoft.commerce.service.asset.AssetTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value = "/asset-types",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AssetTypeController {

    @Autowired
    AssetTypeService assetTypeService;

    @GetMapping
    public ResponseEntity<List<AssetTypeResponseDTO>> getAllAssetTypes() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(assetTypeService.getAllAssetTypes());
    }
}
