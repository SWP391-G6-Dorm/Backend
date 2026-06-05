package com.dormitory.controller;

import com.dormitory.dto.request.LandlordVerifyInfoRequest;
import com.dormitory.dto.response.ApiResponse;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints for Landlord verification:
 *   - PUT /api/landlords/me/verify-info  (LANDLORD submits their own CCCD after Google login)
 *   - PUT /api/admin/landlords/{id}/verify  (ADMIN approves)
 *   - PUT /api/admin/landlords/{id}/reject  (ADMIN rejects)
 */
@RestController
public class LandlordVerificationController {

    private final UserRepository userRepository;

    public LandlordVerificationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── LANDLORD self-service: submit identity info ───────────────────────────

    /**
     * PUT /api/landlords/me/verify-info
     * Called by LANDLORD who registered via Google and needs to submit CCCD/identity info.
     * Requires valid JWT (authenticated LANDLORD user).
     */
    @PreAuthorize("hasAuthority('LANDLORD')")
    @PutMapping("/api/landlords/me/verify-info")
    public ResponseEntity<ApiResponse<Void>> submitVerifyInfo(
            @Valid @RequestBody LandlordVerifyInfoRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getRole() != User.Role.LANDLORD) {
            throw new RuntimeException("Only Landlord accounts can submit verification info.");
        }

        user.setIdentityNumber(request.getIdentityNumber());
        user.setTaxCode(request.getTaxCode());
        user.setBusinessLicense(request.getBusinessLicense());
        userRepository.save(user);

        System.out.println("[DEV] Landlord " + user.getEmail() + " submitted identity info: " + request.getIdentityNumber());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── ADMIN: approve / reject ───────────────────────────────────────────────

    /**
     * PUT /api/admin/landlords/{userId}/verify
     * Approves a landlord account — sets landlordVerified = true.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/api/admin/landlords/{userId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyLandlord(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getRole() != User.Role.LANDLORD) {
            throw new RuntimeException("This user is not a Landlord.");
        }

        user.setLandlordVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * PUT /api/admin/landlords/{userId}/reject
     * Rejects / revokes landlord verification.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/api/admin/landlords/{userId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectLandlord(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getRole() != User.Role.LANDLORD) {
            throw new RuntimeException("This user is not a Landlord.");
        }

        user.setLandlordVerified(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
