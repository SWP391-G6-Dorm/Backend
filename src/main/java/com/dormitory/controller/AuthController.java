package com.dormitory.controller;

import com.dormitory.dto.request.GoogleAuthRequest;
import com.dormitory.dto.request.LoginRequest;
import com.dormitory.dto.request.OtpVerifyRequest;
import com.dormitory.dto.request.RegisterRequest;
import com.dormitory.dto.request.ResendOtpRequest;
import com.dormitory.dto.response.ApiResponse;
import com.dormitory.dto.response.AuthResponse;
import com.dormitory.dto.response.OtpVerifyResponse;
import com.dormitory.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    // ── Google OAuth ──────────────────────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        AuthResponse response = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * POST /api/auth/register
     * Creates a new user with status=PENDING and sends a 6-digit OTP to the console (dev mode).
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── OTP Verification ──────────────────────────────────────────────────────

    /**
     * POST /api/auth/verify-otp
     * Verifies the OTP and activates the user account (status=ACTIVE).
     * Returns role + landlordVerified so frontend can redirect correctly.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        OtpVerifyResponse result = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/auth/resend-otp
     * Generates and resends a new OTP for a PENDING account.
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
