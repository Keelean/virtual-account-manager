package com.keelean.legacy.customeraccounts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VirtualAccountFactoryProvider {

    @Autowired
    private ApplicationContext context;

    AbstractVirtualAccount getVirtualAccount(String virtualAccountType){
        return context.getBean(virtualAccountType, AbstractVirtualAccount.class);
    }
}
