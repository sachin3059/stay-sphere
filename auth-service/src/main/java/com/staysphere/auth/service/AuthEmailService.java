package com.staysphere.auth.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class AuthEmailService {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    @Value("${sendgrid.from-email:noreply@staysphere.local}")
    private String fromEmail;

    @Value("${sendgrid.from-name:StaySphere}")
    private String fromName;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "Reset your StaySphere password";
        String body = """
                You requested a password reset for your StaySphere account.

                Open this link to choose a new password (valid for a limited time):
                %s

                If you did not request this, you can ignore this email.
                """.formatted(resetUrl);

        if (apiKey == null || apiKey.isBlank()) {
            log.info(
                    "SendGrid not configured; password reset link for {}: {}",
                    to,
                    resetUrl);
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Password reset email sent to {}", to);
            } else {
                log.error(
                        "SendGrid error {} sending reset email: {}",
                        response.getStatusCode(),
                        response.getBody());
            }
        } catch (IOException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    public void sendVerificationEmail(String to, String verifyUrl) {
        String subject = "Verify your StaySphere email";
        String body = """
                Welcome to StaySphere!

                Confirm your email address by opening this link:
                %s

                If you did not create an account, you can ignore this email.
                """.formatted(verifyUrl);

        if (apiKey == null || apiKey.isBlank()) {
            log.info(
                    "SendGrid not configured; email verification link for {}: {}",
                    to,
                    verifyUrl);
            return;
        }

        sendPlainText(to, subject, body);
    }

    private void sendPlainText(String to, String subject, String body) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent to {}", to);
            } else {
                log.error(
                        "SendGrid error {}: {}",
                        response.getStatusCode(),
                        response.getBody());
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
