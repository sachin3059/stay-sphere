package com.staysphere.auth.service;

import com.staysphere.auth.dto.AdminUserResponse;
import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.UserRepository;
import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateRole(String userId, String roleRaw) {
        if (roleRaw == null || roleRaw.isBlank()) {
            throw new BadRequestException("Role is required");
        }
        User.Role role;
        try {
            role = User.Role.valueOf(roleRaw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Role must be GUEST, HOST, or ADMIN");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    private AdminUserResponse toResponse(User user) {
        boolean socialOnly = user.getPassword() == null || user.getPassword().isBlank();
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .socialOnly(socialOnly)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
