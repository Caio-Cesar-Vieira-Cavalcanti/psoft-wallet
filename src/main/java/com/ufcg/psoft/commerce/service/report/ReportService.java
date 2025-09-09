package com.ufcg.psoft.commerce.service.report;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;

import java.util.List;

public interface ReportService {
    List<OperationReportResponseDTO> listOperations(OperationReportRequestDTO opRequestDTO);
}
