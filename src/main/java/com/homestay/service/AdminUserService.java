package com.homestay.service;

import com.homestay.dto.request.AdminUpdateUserRequest;
import com.homestay.dto.response.AdminUserResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * SCR-50/51 - Manager/Customer Directory (Admin).
 * Reuse UserRepository.findByRoleWithFilters.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> listUsers(String role, String status, String keyword, Pageable pageable) {
        User.Role roleFilter = parseRole(role);
        if (roleFilter == null) {
            throw new BusinessException("role khong hop le");
        }
        User.Status statusFilter = parseStatus(status);
        String search = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<User> page = userRepository.findByRoleWithFilters(roleFilter, statusFilter, search, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(AdminUserResponse::fromEntity).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi ID: " + id));
        return AdminUserResponse.fromEntity(user);
    }

    @Transactional
    public AdminUserResponse updateUser(UUID id, AdminUpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi ID: " + id));

        if (req.getRole() != null) {
            User.Role role = parseRole(req.getRole());
            if (role == null) {
                throw new BusinessException("role khong hop le");
            }
            user.setRole(role);
        }
        if (req.getStatus() != null) {
            User.Status status = parseStatus(req.getStatus());
            if (status == null) {
                throw new BusinessException("status khong hop le");
            }
            user.setStatus(status);
        }

        User saved = userRepository.save(user);
        return AdminUserResponse.fromEntity(saved);
    }

    private User.Role parseRole(String role) {
        if (role == null || role.isBlank()) return null;
        try {
            return User.Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private User.Status parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return User.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}