package com.staysphere.auth.service;

import com.staysphere.auth.dto.AuthResponse;
import com.staysphere.auth.dto.LoginRequest;
import com.staysphere.auth.dto.RegisterRequest;
import com.staysphere.auth.entity.RefreshToken;
import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.UserRepository;
import com.staysphere.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private static final long ACCESS_TOKEN_EXPIRY = 900000;
    private static final long REFRESH_TOKEN_EXPIRY = 604800000;

    /**
     * Public registration always creates GUEST accounts.
     * HOST and ADMIN roles require a separate admin-provisioned flow.
     */
    private User.Role resolveRegistrationRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return User.Role.GUEST;
        }
        String normalized = requestedRole.trim().toUpperCase();
        if (normalized.equals("GUEST")) {
            return User.Role.GUEST;
        }
        throw new IllegalArgumentException(
                "Invalid role for registration. New users are registered as GUEST.");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User.Role role = resolveRegistrationRole(request.getRole());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .build();

        User saved = userRepository.save(user);

        String accessToken = jwtUtil.generateToken(
                saved.getEmail(), saved.getRole().name());

        RefreshToken refreshToken = refreshTokenService
                .createRefreshToken(saved.getId(), saved.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .fullName(saved.getFullName())
                .accessTokenExpiry(ACCESS_TOKEN_EXPIRY)
                .refreshTokenExpiry(REFRESH_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = refreshTokenService
                .createRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .accessTokenExpiry(ACCESS_TOKEN_EXPIRY)
                .refreshTokenExpiry(REFRESH_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenService
                .validateRefreshToken(refreshToken);

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        // Rotate refresh token — revoke old, create new
        refreshTokenService.revokeToken(refreshToken);
        RefreshToken newRefreshToken = refreshTokenService
                .createRefreshToken(user.getId(), user.getEmail());

        log.info("Token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .accessTokenExpiry(ACCESS_TOKEN_EXPIRY)
                .refreshTokenExpiry(REFRESH_TOKEN_EXPIRY)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
        log.info("User logged out successfully");
    }
}