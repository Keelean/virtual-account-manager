package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.entity.Account;

import java.util.Collections;
import java.util.List;

public interface AccountCreationMode {

    default BaseAccountResponseDto preCreation(Account virtualAccount){
        return BaseAccountResponseDto.builder().build();
    }
    default List<BaseAccountResponseDto> preCreationBulkMode(List<BaseAccountRequestDto> requestDtos){
        return Collections.emptyList();
    }

    default BaseAccountResponseDto singleFullCreation(Account virtualAccountCustomer){
        return BaseAccountResponseDto.builder().build();
    }

    default List<BaseAccountResponseDto> preCreation(List<Account> virtualAccounts){
        return Collections.emptyList();
    }
}
