package com.kaiser.rankbot.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.kaiser.rankbot")  // 💡 Stelle sicher, dass alle Beans erfasst werden!
@SpringBootApplication(scanBasePackages = "com.kaiser.rankbot")

public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

