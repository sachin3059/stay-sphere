package com.staysphere.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmtpPlainTextMailSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${staysphere.mail.from-email:noreply@staysphere.local}")
    private String fromEmail;

    @Value("${staysphere.mail.from-name:StaySphere}")
    private String fromName;

    public SmtpPlainTextMailSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public boolean isConfigured() {
        return smtpHost != null && !smtpHost.isBlank()
                && mailSenderProvider.getIfAvailable() != null;
    }

    public boolean send(String to, String subject, String body) {
        if (!isConfigured()) {
            return false;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(formatFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            log.info("Email sent via SMTP to {}", to);
            return true;
        } catch (Exception e) {
            log.error("SMTP send failed to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private String formatFrom() {
        if (fromName == null || fromName.isBlank()) {
            return fromEmail;
        }
        return fromName + " <" + fromEmail + ">";
    }
}
