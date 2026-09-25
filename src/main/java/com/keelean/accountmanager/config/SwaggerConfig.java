package com.keelean.accountmanager.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("virtual-account-manager")
                .packagesToScan("com.keelean.accountmanager")
                .build();
    }
}
