package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;

import java.util.Collections;
import java.util.List;

public interface VirtualAccountCreationMode {
    default BaseVirtualAccountResponseDto preCreation(VirtualAccount virtualAccount){
        return BaseVirtualAccountResponseDto.builder().build();
    }
    default List<BaseVirtualAccountResponseDto> preCreationBulkMode(List<BaseVirtualAccountRequestDto> requestDtos){
        return Collections.emptyList();
    }

    default BaseVirtualAccountResponseDto singleFullCreation(VirtualAccount virtualAccountCustomer){
        return BaseVirtualAccountResponseDto.builder().build();
    }

    default List<BaseVirtualAccountResponseDto> preCreation(List<VirtualAccount> virtualAccounts){
        return Collections.emptyList();
    }
}
