package com.staysphere.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {
        "com.staysphere.payment.entity",
        "com.staysphere.payment.outbox"
})
@EnableJpaRepositories(basePackages = {
        "com.staysphere.payment.repository",
        "com.staysphere.payment.outbox"
})
@ComponentScan(basePackages = {
    "com.staysphere.payment",
    "com.staysphere.common"
})
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
