package com.keelean.legacy.customeraccounts.service.impl;

import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;
import com.keelean.legacy.customeraccounts.mapper.VirtualAccountCustomerMapper;
import com.keelean.legacy.customeraccounts.service.AbstractVirtualAccount;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class VirtualAccountRecursiveTask extends RecursiveTask<List<BaseVirtualAccountResponseDto>> {
    private static final int THRESHOLD = 25;

    int low;
    int high;
    List<BaseVirtualAccountRequestDto> virtualAccounts;
    AbstractVirtualAccount abstractVirtualAccount;
    VirtualAccountCustomerMapper virtualAccountCustomerMapper;

    List<BaseVirtualAccountResponseDto> virtualAccountList = new ArrayList<>();

    List<BaseVirtualAccountRequestDto> accountRequestDtos;


    public VirtualAccountRecursiveTask(AbstractVirtualAccount abstractVirtualAccount, VirtualAccountCustomerMapper virtualAccountCustomerMapper, List<BaseVirtualAccountRequestDto> accountRequestDtos){
        this.abstractVirtualAccount = abstractVirtualAccount;
        this.virtualAccountCustomerMapper = virtualAccountCustomerMapper;
        this.accountRequestDtos = accountRequestDtos;
    }

    private BaseVirtualAccountResponseDto generateSingleVirtualAccountId(BaseVirtualAccountRequestDto bvac) {
        BaseVirtualAccountResponseDto virtualAccountResponseDto = null;
        try {
            VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(bvac);
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
    protected List<BaseVirtualAccountResponseDto> compute() {
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

    private List<BaseVirtualAccountResponseDto> generateBulkVirtualAccountIds() {
        log.info("accountRequestDtos0::{}",accountRequestDtos.size());
        VirtualAccount virtualAccount = null;
        for (BaseVirtualAccountRequestDto bvac: accountRequestDtos) {
            try {
                //BaseVirtualAccountResponseDto virtualAccountResponseDto;
                virtualAccount = abstractVirtualAccount.createVirtualAccount(bvac);
                log.info("REFID::{}",bvac.getReferenceId());
                log.info("VARTID::{}",virtualAccount.getAccountId());
                if(virtualAccount.getTimeoutInMins() != null){
                    BaseVirtualAccountResponseDto virtualAccountResponseDto = abstractVirtualAccount.singleFullCreation(virtualAccount);
                    virtualAccountList.add(virtualAccountResponseDto);
                }else {
                    BaseVirtualAccountResponseDto virtualAccountResponseDto = abstractVirtualAccount.preCreation(virtualAccount);
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
