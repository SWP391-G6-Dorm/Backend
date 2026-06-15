package com.dormitory.service;

import com.dormitory.dto.request.AdminUpdateUserRequest;
import com.dormitory.dto.response.AdminUserDto;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
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
        User.Role roleEnum = (role != null && !role.equals("ALL")) ? User.Role.valueOf(role) : null;
        User.Status statusEnum = (status != null && !status.equals("ALL")) ? User.Status.valueOf(status) : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword : null;

        Page<User> users = userRepository.searchUsers(roleEnum, statusEnum, kw, PageRequest.of(page, size));
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
