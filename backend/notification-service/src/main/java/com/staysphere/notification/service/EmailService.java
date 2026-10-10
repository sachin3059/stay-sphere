package com.staysphere.notification.service;

import com.staysphere.common.mail.SmtpPlainTextMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final SmtpPlainTextMailSender smtpMailSender;

    public boolean sendEmail(String to, String subject, String body) {
        if (smtpMailSender.send(to, subject, body)) {
            return true;
        }
        log.info("SMTP not configured; skipping email to {}", to);
        return false;
    }
}
