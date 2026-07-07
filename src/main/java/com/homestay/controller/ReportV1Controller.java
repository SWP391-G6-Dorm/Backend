package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.BookingStatusCountResponse;
import com.homestay.dto.response.PropertyKpisResponse;
import com.homestay.entity.User;
import com.homestay.service.PropertyKpisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** SCR-27 — Manager reporting v1 endpoints (FR-16). */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportV1Controller {

    private final PropertyKpisService propertyKpisService;

    @GetMapping("/property-kpis")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PropertyKpisResponse>> getPropertyKpis(
            @RequestParam UUID propertyId,
            @AuthenticationPrincipal User currentUser) {
        PropertyKpisResponse data = propertyKpisService.getPropertyKpis(currentUser, propertyId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/booking-status-breakdown")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<BookingStatusCountResponse>>> getBookingStatusBreakdown(
            @RequestParam UUID propertyId,
            @AuthenticationPrincipal User currentUser) {
        List<BookingStatusCountResponse> data =
                propertyKpisService.getBookingStatusBreakdown(currentUser, propertyId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
