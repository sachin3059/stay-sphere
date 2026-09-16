package com.staysphere.property;

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
        "com.staysphere.property.entity",
        "com.staysphere.availability.entity",
        "com.staysphere.pricing.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.staysphere.property.repository",
        "com.staysphere.availability.repository",
        "com.staysphere.pricing.repository"
})
@ComponentScan(basePackages = {
        "com.staysphere.property",
        "com.staysphere.availability",
        "com.staysphere.pricing",
        "com.staysphere.common"
})
public class PropertyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyServiceApplication.class, args);
    }
}
