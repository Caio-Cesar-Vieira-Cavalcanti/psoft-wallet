package com.ufcg.psoft.commerce.service.report.fetchers;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;

import java.util.List;

public interface OperationFetcher<T> {
    List<OperationReportResponseDTO> fetch(OperationReportRequestDTO opRequestDTO);
}
