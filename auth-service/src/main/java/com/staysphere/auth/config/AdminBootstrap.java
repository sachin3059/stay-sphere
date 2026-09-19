package com.staysphere.auth.config;

import com.staysphere.auth.entity.User;
import com.staysphere.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap {

    private final UserRepository userRepository;

    @Value("${staysphere.admin.bootstrap-email:}")
    private String bootstrapAdminEmail;

    @EventListener(ApplicationReadyEvent.class)
    public void promoteBootstrapAdmin() {
        if (bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()) {
            return;
        }
        String email = bootstrapAdminEmail.trim().toLowerCase();
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if (user.getRole() == User.Role.ADMIN) {
                return;
            }
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
            log.info("Promoted {} to ADMIN (bootstrap email)", email);
        }, () -> log.warn(
                "staysphere.admin.bootstrap-email={} — no user yet; register first, then restart auth-service",
                email));
    }
}
