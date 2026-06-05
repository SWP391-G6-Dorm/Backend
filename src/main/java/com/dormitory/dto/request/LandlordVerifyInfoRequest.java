package com.dormitory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for LANDLORD submitting verification info after Google OAuth registration.
 * Endpoint: PUT /api/landlords/me/verify-info (requires JWT)
 */
@Data
public class LandlordVerifyInfoRequest {

    @NotBlank(message = "CCCD / Identity number is required")
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;

    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;

    @Size(max = 255, message = "Business license must not exceed 255 characters")
    private String businessLicense;
}
