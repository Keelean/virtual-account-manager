package com.keelean.accountmanager.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AccountPoolContext {

    private final ApplicationContext context;

    public AccountPoolService getAccountPool(String poolName) {
        return context.getBean(poolName, AccountPoolService.class);
    }
}
