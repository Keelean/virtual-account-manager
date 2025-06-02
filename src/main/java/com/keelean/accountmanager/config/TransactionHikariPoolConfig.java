package com.keelean.accountmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
@Component
public class TransactionHikariPoolConfig {
    private long connectionTimeout = 40000;
    private int minimumIdle = 10;
    private int maximumPoolSize = 20;
    private long idleTimeout = 240000;
    private long maxLifetime = 240000;
    private String poolName = "ACCOUNT_MANAGER_POOL";
}
