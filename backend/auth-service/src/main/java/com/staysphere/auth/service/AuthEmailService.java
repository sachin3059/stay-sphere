package com.staysphere.auth.service;

import com.staysphere.common.mail.SmtpPlainTextMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthEmailService {

    private final SmtpPlainTextMailSender smtpMailSender;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "Reset your StaySphere password";
        String body = """
                You requested a password reset for your StaySphere account.

                Open this link to choose a new password (valid for a limited time):
                %s

                If you did not request this, you can ignore this email.
                """.formatted(resetUrl);

        if (smtpMailSender.send(to, subject, body)) {
            return;
        }
        log.info(
                "SMTP not configured; password reset link for {}: {}",
                to,
                resetUrl);
    }

    public void sendVerificationEmail(String to, String verifyUrl) {
        String subject = "Verify your StaySphere email";
        String body = """
                Welcome to StaySphere!

                Confirm your email address by opening this link:
                %s

                If you did not create an account, you can ignore this email.
                """.formatted(verifyUrl);

        if (!smtpMailSender.send(to, subject, body)) {
            log.info(
                    "SMTP not configured; email verification link for {}: {}",
                    to,
                    verifyUrl);
        }
    }
}
