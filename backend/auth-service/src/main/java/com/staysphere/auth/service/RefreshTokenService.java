package com.staysphere.auth.service;

import com.staysphere.auth.entity.RefreshToken;
import com.staysphere.auth.repository.RefreshTokenRepository;
import com.staysphere.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Transactional
    public RefreshToken createRefreshToken(String userId,
                                           String email) {
        // Revoke all existing tokens for this user
        refreshTokenRepository.revokeAllUserTokens(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .email(email)
                .expiresAt(LocalDateTime.now()
                        .plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .build();

        RefreshToken saved = refreshTokenRepository
                .save(refreshToken);
        log.info("Refresh token created for user: {}", email);
        return saved;
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException(
                        "Refresh token not found"));

        if (!refreshToken.isValid()) {
            throw new BadRequestException(
                    refreshToken.isRevoked() ?
                            "Refresh token has been revoked" :
                            "Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.revokeToken(token);
        log.info("Refresh token revoked");
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("All tokens revoked for user: {}", userId);
    }
}