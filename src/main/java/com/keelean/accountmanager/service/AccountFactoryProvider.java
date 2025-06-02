package com.keelean.accountmanager.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AccountFactoryProvider {

    private final ApplicationContext context;

    AbstractVirtualAccount getVirtualAccount(String virtualAccountType){
        return context.getBean(virtualAccountType, AbstractVirtualAccount.class);
    }
}
