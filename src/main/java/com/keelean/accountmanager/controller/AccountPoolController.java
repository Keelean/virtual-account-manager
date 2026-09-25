package com.keelean.accountmanager.controller;

import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.dto.AccountPoolDto;
import com.keelean.accountmanager.dto.AccountPoolResponse;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.service.AccountPoolContext;
import com.keelean.accountmanager.service.AccountPoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping(AppConstants.ACCOUNT_POOL_BASE_URL)
@AllArgsConstructor
public class AccountPoolController {

    private final AccountPoolContext accountPoolContext;

    @PostMapping
    public AccountPoolResponse createAccountPool(@RequestBody @Valid AccountPoolDto accountPoolDto) {
        AccountPoolService accountPoolService = accountPoolContext.getAccountPool(accountPoolDto.getPoolType().name().toLowerCase());
        AccountPool accountPool = accountPoolService.createAccountPool(accountPoolDto);
        return AccountPoolResponse.builder()
                .poolSequence(String.valueOf(accountPool.getStartPrefix()))
                .poolType(accountPoolDto.getPoolType().name())
                .build();
    }
}
