package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.ContractSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.User;
import com.homestay.service.ContractService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** SCR-21 — Customer contracts (api-spec v1). */
@RestController
@RequestMapping("/api/v1/contracts")
public class ContractV1Controller {

    private final ContractService contractService;

    public ContractV1Controller(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> getMyContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser) {
        PageResponse<ContractSummaryResponse> data =
                contractService.getMyContracts(currentUser, page, size, status, search, sort);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> downloadContractPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        byte[] pdfBytes = contractService.downloadContractPdf(id, currentUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Contract_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
