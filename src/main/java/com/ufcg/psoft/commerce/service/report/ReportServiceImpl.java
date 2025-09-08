package com.ufcg.psoft.commerce.service.report;

import com.ufcg.psoft.commerce.dto.report.OperationReportRequestDTO;
import com.ufcg.psoft.commerce.dto.report.OperationReportResponseDTO;
import com.ufcg.psoft.commerce.repository.client.ClientRepository;
import com.ufcg.psoft.commerce.service.admin.AdminService;
import com.ufcg.psoft.commerce.service.report.fetchers.OperationFetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    AdminService adminService;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    List<OperationFetcher<?>> fetchers;

    @Override
    @Transactional(readOnly = true)
    public List<OperationReportResponseDTO> listOperations(OperationReportRequestDTO opRequestDTO) {
        adminService.validateAdmin(opRequestDTO.getAdminEmail(), opRequestDTO.getAdminAccessCode());

        if (opRequestDTO.getClientId() != null && clientRepository.findById(opRequestDTO.getClientId()).isEmpty()) {
            return List.of();
        }

        return fetchers.stream()
                .flatMap(fetcher -> fetcher.fetch(opRequestDTO).stream())
                .sorted(Comparator.comparing(OperationReportResponseDTO::getOccurredAt).reversed())
                .toList();
    }
}
