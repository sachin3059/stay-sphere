package com.staysphere.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EntityScan(basePackages = {
        "com.staysphere.booking.entity",
        "com.staysphere.booking.outbox",
        "com.staysphere.waitlist.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.staysphere.booking.repository",
        "com.staysphere.booking.outbox",
        "com.staysphere.waitlist.repository"
})
@ComponentScan(basePackages = {
        "com.staysphere.booking",
        "com.staysphere.waitlist",
        "com.staysphere.common"
})
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
