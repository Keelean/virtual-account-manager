package com.keelean.accountmanager.service.impl;


import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.mapper.AccountCustomerMapper;
import com.keelean.accountmanager.service.AbstractVirtualAccount;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class VirtualAccountRecursiveTask extends RecursiveTask<List<BaseAccountResponseDto>> {
    private static final int THRESHOLD = 25;

    int low;
    int high;
    List<BaseAccountRequestDto> virtualAccounts;
    AbstractVirtualAccount abstractVirtualAccount;
    AccountCustomerMapper virtualAccountCustomerMapper;

    List<BaseAccountResponseDto> virtualAccountList = new ArrayList<>();

    List<BaseAccountRequestDto> accountRequestDtos;


    public VirtualAccountRecursiveTask(AbstractVirtualAccount abstractVirtualAccount, AccountCustomerMapper virtualAccountCustomerMapper, List<BaseAccountRequestDto> accountRequestDtos){
        this.abstractVirtualAccount = abstractVirtualAccount;
        this.virtualAccountCustomerMapper = virtualAccountCustomerMapper;
        this.accountRequestDtos = accountRequestDtos;
    }

    private BaseAccountResponseDto generateSingleVirtualAccountId(BaseAccountRequestDto bvac) {
        BaseAccountResponseDto virtualAccountResponseDto = null;
        try {
            Account virtualAccount = abstractVirtualAccount.createVirtualAccount(bvac);
            if(virtualAccount.getTimeoutInMins() != null){
                virtualAccountResponseDto = abstractVirtualAccount.singleFullCreation(virtualAccount);
            }else {
                virtualAccountResponseDto = abstractVirtualAccount.preCreation(virtualAccount);
            }
            //virtualAccountList.add(virtualAccountResponseDto);
        }catch (Exception e){
            log.info(e.getMessage());
            //virtualAccountResponseDto = BaseVirtualAccountResponseDto.builder().build();
        }
        return virtualAccountResponseDto;
    }

    @Override
    protected List<BaseAccountResponseDto> compute() {
        if (accountRequestDtos.size() > THRESHOLD) {

            log.info("accountRequestDtos::{}", accountRequestDtos.size());

            var leftList = this.accountRequestDtos.subList(0, this.accountRequestDtos.size() / 2);
            var rightList = this.accountRequestDtos.subList(leftList.size(), this.accountRequestDtos.size());

            VirtualAccountRecursiveTask leftTask = new VirtualAccountRecursiveTask(abstractVirtualAccount,virtualAccountCustomerMapper, leftList);
            VirtualAccountRecursiveTask rightTask = new VirtualAccountRecursiveTask(abstractVirtualAccount,virtualAccountCustomerMapper, rightList);
            leftTask.fork();

            var rightResult = rightTask.compute();
            log.info("rightResult::{}", rightResult.size());
            var leftResult = leftTask.join();
            log.info("leftResult::{}", leftResult.size());
            virtualAccountList.addAll(rightResult);
            virtualAccountList.addAll(leftResult);

            return virtualAccountList;
        } else {
            return generateBulkVirtualAccountIds();
        }
    }

    private List<BaseAccountResponseDto> generateBulkVirtualAccountIds() {
        log.info("accountRequestDtos0::{}",accountRequestDtos.size());
        Account virtualAccount = null;
        for (BaseAccountRequestDto bvac: accountRequestDtos) {
            try {
                //BaseVirtualAccountResponseDto virtualAccountResponseDto;
                virtualAccount = abstractVirtualAccount.createVirtualAccount(bvac);
                log.info("REFID::{}",bvac.getReferenceId());
                log.info("VARTID::{}",virtualAccount.getAccountId());
                if(virtualAccount.getTimeoutInMins() != null){
                    BaseAccountResponseDto virtualAccountResponseDto = abstractVirtualAccount.singleFullCreation(virtualAccount);
                    virtualAccountList.add(virtualAccountResponseDto);
                }else {
                    BaseAccountResponseDto virtualAccountResponseDto = abstractVirtualAccount.preCreation(virtualAccount);
                    virtualAccountList.add(virtualAccountResponseDto);
                }
            }catch (Exception e){
                log.info("EXCEPTION::"+e.getMessage());
                virtualAccount = null;
            }
            virtualAccount = null;
        }
        return virtualAccountList;
    }

}
