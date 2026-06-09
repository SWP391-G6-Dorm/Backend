package com.dormitory.controller;

import com.dormitory.dto.request.AdminUpdateUserRequest;
import com.dormitory.dto.response.AdminUserDto;
import com.dormitory.dto.response.ApiResponse;
import com.dormitory.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Page<AdminUserDto> result = adminUserService.searchUsers(keyword, role, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        String adminIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID adminId = UUID.fromString(adminIdStr);

        AdminUserDto result = adminUserService.updateUser(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
