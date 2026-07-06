package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.entity.User;
import com.homestay.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** SCR-20 — Customer payment (api-spec v1). */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller {

    private final PaymentService paymentService;

    public PaymentV1Controller(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/vnpay/create-url")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentUrl(
            @RequestParam UUID bookingId,
            @RequestParam String type,
            @AuthenticationPrincipal User currentUser) {
        Map<String, String> result = paymentService.createVnpayPaymentUrl(bookingId, type, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Tạo URL thanh toán thành công", result));
    }
}
