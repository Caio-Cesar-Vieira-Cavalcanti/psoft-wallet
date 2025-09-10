package com.ufcg.psoft.commerce.controller.report;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.service.report.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value =  "/reports",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ReportController {

    @Autowired
    ReportService reportService;

    @PostMapping("/operations")
    public ResponseEntity<List<OperationReportResponseDTO>> listOperations(
            @RequestBody @Valid OperationReportRequestDTO opRequestDTO
            ) {
        List<OperationReportResponseDTO> operationsByClients = reportService.listOperations(opRequestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(operationsByClients);
    }
}
