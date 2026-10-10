package com.staysphere.auth.service;

import com.staysphere.auth.config.AuthPasswordResetProperties;
import com.staysphere.auth.entity.PasswordResetToken;
import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.PasswordResetTokenRepository;
import com.staysphere.auth.repository.UserRepository;
import com.staysphere.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthEmailService authEmailService;
    private final AuthPasswordResetProperties resetProperties;

    /**
     * Always succeeds from the client's perspective (no email enumeration).
     */
    @Transactional
    public void requestReset(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return;
        }
        String email = rawEmail.trim();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                log.debug(
                        "Password reset skipped for OAuth-only account {}",
                        email);
                return;
            }
            issueResetToken(user);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        validateNewPassword(newPassword);
        String token = rawToken != null ? rawToken.trim() : "";
        if (token.isEmpty()) {
            throw new BadRequestException("Reset token is required");
        }

        String hash = sha256Hex(token);
        PasswordResetToken reset = tokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired reset link"));

        if (!reset.isValid()) {
            throw new BadRequestException("Invalid or expired reset link");
        }

        User user = userRepository.findById(reset.getUserId())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired reset link"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        reset.setUsedAt(LocalDateTime.now());
        tokenRepository.save(reset);
        refreshTokenService.revokeAllUserTokens(user.getId());

        log.info("Password reset completed for user {}", user.getEmail());
    }

    private void issueResetToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidateActiveTokensForUser(user.getId(), now);

        String plainToken = generateToken();
        String hash = sha256Hex(plainToken);
        LocalDateTime expires = now.plusMinutes(resetProperties.getTokenTtlMinutes());

        tokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(hash)
                .expiresAt(expires)
                .build());

        String base = resetProperties.getFrontendBaseUrl().replaceAll("/$", "");
        String resetUrl = base + "/reset-password?token=" + plainToken;
        authEmailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    private static void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH
                            + " characters");
        }
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
