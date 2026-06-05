package com.dormitory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned after successful OTP verification.
 * Frontend uses 'role' to decide which page to redirect to:
 *   TENANT → /login
 *   LANDLORD → /landlord-pending
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyResponse {
    private String role;
    private boolean landlordVerified;
    private String message;
}
