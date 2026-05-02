package com.threatintel.extraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ExtractionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExtractionApplication.class, args);
    }
}
