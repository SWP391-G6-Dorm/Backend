package com.dormitory.dto.request;

import com.dormitory.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotNull(message = "Role is required")
    private User.Role role;

    // ── LANDLORD-specific fields (optional for TENANT) ────────────────────────

    /** CCCD / Passport (required when role=LANDLORD, validated in service layer) */
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;

    /** Mã số thuế (optional) */
    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;

    /** Giấy phép kinh doanh (optional) */
    @Size(max = 255, message = "Business license must not exceed 255 characters")
    private String businessLicense;
}
