package com.staysphere.notification.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    public boolean sendEmail(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("SendGrid not configured; skipping email to {}", to);
            return false;
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

            if (response.getStatusCode() >= 200
                    && response.getStatusCode() < 300) {
                log.info("Email sent via SendGrid to: {} status: {}",
                        to, response.getStatusCode());
                return true;
            } else {
                log.error("SendGrid error: {} — {}",
                        response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (IOException e) {
            log.error("SendGrid exception sending to {}: {}",
                    to, e.getMessage());
            return false;
        }
    }
}