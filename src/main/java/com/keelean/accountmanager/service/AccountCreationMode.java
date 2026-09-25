package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;

public interface AccountCreationMode {

    default BaseAccountResponseDto preCreation(Account virtualAccount){
        return BaseAccountResponseDto.builder().build();
    }

    default BaseAccountResponseDto singleFullCreation(Account virtualAccountCustomer){
        return BaseAccountResponseDto.builder().build();
    }

    // The unsaved row singleFullCreation stores; bulk requests save these together in one transaction
    default AccountCustomer toFullCustomer(Account virtualAccount){
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support full creation");
    }

    // The unsaved row preCreation stores; bulk requests save these together in one transaction
    default AccountCustomer toPreCreatedCustomer(Account virtualAccount){
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support pre-creation");
    }
}
