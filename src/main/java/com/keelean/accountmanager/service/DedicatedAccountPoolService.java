package com.keelean.accountmanager.service;



import com.keelean.accountmanager.dto.AccountPoolDto;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import com.keelean.accountmanager.repo.PartnerAccountConfigRepo;
import com.keelean.accountmanager.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("dedicated")
@Slf4j
public class DedicatedAccountPoolService extends AccountPoolService {

    private PartnerAccountConfigRepo partnerAccountConfigRepo;

    @Override
    public AccountPool createPool(AccountPoolDto accountPoolDto) {

        //Check if account pool exist in account pool
        //If it does not exist, create one
        Optional<AccountPool> optionalAccountPool = accountPoolRepo.findByPrefixSeries(accountPoolDto.getPrefixSeries());
        if (optionalAccountPool.isEmpty()) {
            AccountPool accountPool = AccountPool.builder()
                    .state(State.OPEN)
                    .capacity(accountPoolDto.getCapacity())
                    .prefixSeries(accountPoolDto.getPrefixSeries())
                    .startPrefix(Integer.parseInt(AppUtils.formatStartSequence(AccountCapacity.getStartPrefixWidth(accountPoolDto.getCapacity()),
                            accountPoolDto.getPrefixSeries(), 0)))
                    .startPrefixCount(0)//Take this out
                    .build();
            return saveOrUpdatePool(accountPool);
        }
        //If it exist
        throw new IllegalArgumentException("Prefix series for capacity of" + accountPoolDto.getCapacity().getDesc() + " is already exist!");
    }

}
