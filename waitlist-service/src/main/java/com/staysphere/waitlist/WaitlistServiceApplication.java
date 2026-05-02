package com.staysphere.waitlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "com.staysphere.waitlist",
    "com.staysphere.common"
})
public class WaitlistServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaitlistServiceApplication.class, args);
    }
}
