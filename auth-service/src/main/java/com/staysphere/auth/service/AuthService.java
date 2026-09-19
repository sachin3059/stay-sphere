package com.staysphere.auth.service;

import com.staysphere.auth.dto.AuthResponse;
import com.staysphere.auth.dto.LoginRequest;
import com.staysphere.auth.dto.RegisterRequest;
import com.staysphere.auth.entity.RefreshToken;
import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.UserRepository;
import com.staysphere.auth.service.GitHubOAuthService.VerifiedGitHubUser;
import com.staysphere.auth.service.GoogleOAuthService.VerifiedGoogleUser;
import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.exception.ConflictException;
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
    private final GoogleOAuthService googleOAuthService;
    private final GitHubOAuthService gitHubOAuthService;

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
            throw new ConflictException("Email already registered");
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
        return issueTokens(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("Invalid credentials"));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException(
                    "This account uses social sign-in. Continue with Google or GitHub.");
        }
        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        VerifiedGoogleUser verified = googleOAuthService.verifyIdToken(idToken);
        return issueTokens(resolveGoogleUser(verified));
    }

    @Transactional
    public AuthResponse loginWithGitHub(String code) {
        VerifiedGitHubUser verified = gitHubOAuthService.exchangeCode(code);
        return issueTokens(resolveGitHubUser(verified));
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenService
                .validateRefreshToken(refreshToken);

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("User not found"));

        refreshTokenService.revokeToken(refreshToken);
        log.info("Token refreshed for user: {}", user.getEmail());
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
        log.info("User logged out successfully");
    }

    /**
     * Lets a GUEST upgrade to HOST so they can manage listings (no SQL required).
     */
    @Transactional
    public AuthResponse becomeHost(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getRole() == User.Role.HOST) {
            return issueTokens(user);
        }
        if (user.getRole() != User.Role.GUEST) {
            throw new BadRequestException(
                    "Only guest accounts can become hosts");
        }

        user.setRole(User.Role.HOST);
        User saved = userRepository.save(user);
        log.info("User {} upgraded to HOST", saved.getEmail());
        return issueTokens(saved);
    }

    private User resolveGoogleUser(VerifiedGoogleUser verified) {
        return userRepository.findByGoogleSub(verified.sub())
                .orElseGet(() -> linkOrCreateOAuthUser(
                        verified.email(),
                        verified.fullName(),
                        verified.sub(),
                        null));
    }

    private User resolveGitHubUser(VerifiedGitHubUser verified) {
        return userRepository.findByGithubId(verified.githubId())
                .orElseGet(() -> linkOrCreateOAuthUser(
                        verified.email(),
                        verified.fullName(),
                        null,
                        verified.githubId()));
    }

    private User linkOrCreateOAuthUser(
            String email,
            String fullName,
            String googleSub,
            String githubId) {
        return userRepository.findByEmail(email).map(existing -> {
            if (googleSub != null) {
                if (existing.getGoogleSub() != null
                        && !existing.getGoogleSub().equals(googleSub)) {
                    throw new ConflictException(
                            "Email already linked to another Google account");
                }
                existing.setGoogleSub(googleSub);
            }
            if (githubId != null) {
                if (existing.getGithubId() != null
                        && !existing.getGithubId().equals(githubId)) {
                    throw new ConflictException(
                            "Email already linked to another GitHub account");
                }
                existing.setGithubId(githubId);
            }
            if (existing.getFullName() == null || existing.getFullName().isBlank()) {
                existing.setFullName(fullName);
            }
            return userRepository.save(existing);
        }).orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .fullName(fullName)
                .role(User.Role.GUEST)
                .googleSub(googleSub)
                .githubId(githubId)
                .build()));
    }

    private AuthResponse issueTokens(User user) {
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
}