package com.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceHealthMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHealthMonitorApplication.class, args);
    }
}
