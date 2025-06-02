package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.AccountPoolDto;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import com.keelean.accountmanager.repo.PartnerAccountConfigRepo;
import com.keelean.accountmanager.utils.AppUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("shared")
@AllArgsConstructor
public class SharedAccountPoolService extends AccountPoolService {

    private final PartnerAccountConfigRepo partnerAccountConfigRepo;

    @Override
    AccountPool createPool(AccountPoolDto accountPoolDto) {

        Optional<AccountPool> accountPoolOptional = accountPoolRepo.findByCapacityAndStartPrefix(accountPoolDto.getCapacity(), accountPoolDto.getPrefixSeries());
        if (accountPoolOptional.isEmpty()) {
            throw new IllegalArgumentException("No existing prefix and capacity to share from!");
        }

        //Get the shared pool enum constant
        Optional<AccountCapacity> sharedCapacity = Arrays.stream(AccountCapacity.values()).filter(c -> c != accountPoolDto.getCapacity()
                && c.getReusableDigits() == accountPoolDto.getCapacity().getReusableDigits()).findFirst();


        AccountPool existingAccountPool = accountPoolOptional.get();

        int totalPossibleAllocations = (int) Math.pow(10.0, AccountCapacity.getStartPrefixWidth(accountPoolDto.getCapacity()));

        Set<String> existingPartnerPrefixes = getAllExistingPartnerStartPrefixes(String.valueOf(accountPoolDto.getPrefixSeries()));
        Set<String> excludedPrefixes = existingAccountPool.getExcludedPrefixStart();
        excludedPrefixes.addAll(existingPartnerPrefixes);

        for (int i = 0; i < totalPossibleAllocations; i++) {
            String sharedPrefixSeries = AppUtils.formatStartSequence(totalPossibleAllocations, accountPoolDto.getPrefixSeries(), i);
            //check if sharedPrefixSeries already assigned
            if (!excludedPrefixes.contains(sharedPrefixSeries)) {
                //create new sequence
                AccountPool newSharedPool = AccountPool.builder()
                        .state(State.OPEN)
                        .capacity(accountPoolDto.getCapacity())
                        .prefixSeries(Integer.parseInt(sharedPrefixSeries))
                        .currentSequence(-1)
                        .startPrefix(Integer.parseInt(sharedPrefixSeries))
                        .capacity(sharedCapacity.orElseThrow())
                        .build();
                existingAccountPool.getExcludedPrefixStart().add(sharedPrefixSeries);
                existingAccountPool.setStartPrefixCount(existingAccountPool.getStartPrefixCount() + 1);
                accountPoolRepo.save(existingAccountPool);
                return saveOrUpdatePool(newSharedPool);
            }
        }
        throw new IllegalArgumentException("There is an existing pool that is still open.");
    }

    private Set<String> getAllExistingPartnerStartPrefixes(String prefixSeries) {
        List<PartnerAccountConfig> partnerAccountConfigs = partnerAccountConfigRepo.findByPrefixStartsWith(prefixSeries);
        return partnerAccountConfigs.stream().map(PartnerAccountConfig::getPrefix).collect(Collectors.toSet());
    }
}
