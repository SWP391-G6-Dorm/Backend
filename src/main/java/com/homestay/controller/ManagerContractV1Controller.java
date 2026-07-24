package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.ContractSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.User;
import com.homestay.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SCR-38 — Contract Management (canonical: GET /api/v1/managers/contracts).
 */
@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerContractV1Controller {

    private final ContractService contractService;

    @GetMapping("/contracts")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<ContractSummaryResponse>>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        PageResponse<ContractSummaryResponse> data = contractService.getManagerContracts(
                currentUser, propertyId, page, size, status, search, sort);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
