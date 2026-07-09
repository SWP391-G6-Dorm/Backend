package com.homestay.dto.response;

import com.homestay.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO response cho SCR-50/51 - Manager/Customer Directory (Admin).
 * Endpoint: GET /api/admin/users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserResponse fromEntity(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().name())
                .status(u.getStatus().name())
                .avatarUrl(u.getAvatarUrl())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}