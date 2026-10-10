package com.staysphere.auth.service;

import com.staysphere.auth.config.AuthEmailVerificationProperties;
import com.staysphere.auth.entity.EmailVerificationToken;
import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.EmailVerificationTokenRepository;
import com.staysphere.auth.repository.UserRepository;
import com.staysphere.auth.util.AuthTokenSupport;
import com.staysphere.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AuthEmailService authEmailService;
    private final AuthEmailVerificationProperties properties;

    @Transactional
    public void sendVerificationEmail(User user) {
        if (user.isEmailVerified()) {
            return;
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return;
        }
        issueTokenAndSend(user);
    }

    @Transactional
    public User verifyEmail(String rawToken) {
        String token = rawToken != null ? rawToken.trim() : "";
        if (token.isEmpty()) {
            throw new BadRequestException("Verification token is required");
        }

        String hash = AuthTokenSupport.sha256Hex(token);
        EmailVerificationToken record = tokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired verification link"));

        if (!record.isValid()) {
            throw new BadRequestException("Invalid or expired verification link");
        }

        User user = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired verification link"));

        if (user.isEmailVerified()) {
            record.setUsedAt(LocalDateTime.now());
            tokenRepository.save(record);
            return user;
        }

        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        record.setUsedAt(LocalDateTime.now());
        tokenRepository.save(record);

        log.info("Email verified for {}", user.getEmail());
        return user;
    }

    @Transactional
    public void resendForEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("User not found");
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException(
                    "This account uses social sign-in; email is verified by your provider.");
        }
        issueTokenAndSend(user);
    }

    private void issueTokenAndSend(User user) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidateActiveTokensForUser(user.getId(), now);

        String plain = AuthTokenSupport.generatePlainToken();
        String hash = AuthTokenSupport.sha256Hex(plain);
        LocalDateTime expires = now.plusHours(properties.getTokenTtlHours());

        tokenRepository.save(EmailVerificationToken.builder()
                .userId(user.getId())
                .tokenHash(hash)
                .expiresAt(expires)
                .build());

        String base = properties.getFrontendBaseUrl().replaceAll("/$", "");
        String verifyUrl = base + "/verify-email?token=" + plain;
        authEmailService.sendVerificationEmail(user.getEmail(), verifyUrl);
    }
}
