package com.dormitory.service;

import com.dormitory.dto.request.AdminUpdateUserRequest;
import com.dormitory.dto.response.AdminUserDto;
import com.homestay.entity.User;
import com.homestay.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<AdminUserDto> searchUsers(String keyword, String role, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasRole    = role   != null && !role.equals("ALL");
        boolean hasStatus  = status != null && !status.equals("ALL");

        User.Role roleEnum   = hasRole   ? User.Role.valueOf(role)     : null;
        User.Status statusEnum = hasStatus ? User.Status.valueOf(status) : null;

        Page<User> users;

        if (hasKeyword) {
            // keyword search — role filter applied per-param; fall back to CUSTOMER if unspecified
            User.Role r = roleEnum != null ? roleEnum : User.Role.CUSTOMER;
            users = userRepository
                    .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                            r, keyword, r, keyword, pageable);
        } else if (hasRole && hasStatus) {
            users = userRepository.findByRoleAndStatus(roleEnum, statusEnum, pageable);
        } else if (hasRole) {
            users = userRepository.findByRole(roleEnum, pageable);
        } else if (hasStatus) {
            users = userRepository.findByRoleAndStatus(User.Role.CUSTOMER, statusEnum, pageable);
        } else {
            users = userRepository.findByRole(User.Role.CUSTOMER, pageable);
        }

        return users.map(AdminUserDto::new);
    }

    public AdminUserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new AdminUserDto(user);
    }

    public AdminUserDto updateUser(UUID targetId, AdminUpdateUserRequest req, UUID adminId) {
        if (targetId.equals(adminId) && "SUSPENDED".equals(req.getStatus())) {
            throw new RuntimeException("System Admins cannot deactivate themselves");
        }

        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(User.Role.valueOf(req.getRole()));
        user.setStatus(User.Status.valueOf(req.getStatus()));
        userRepository.save(user);

        return new AdminUserDto(user);
    }
}
