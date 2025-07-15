package com.ufcg.psoft.commerce.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/asset",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class AssetController {
    // crud de ativos
}
