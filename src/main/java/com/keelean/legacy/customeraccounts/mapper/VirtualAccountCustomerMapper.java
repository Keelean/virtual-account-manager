package com.keelean.legacy.customeraccounts.mapper;

import com.keelean.legacy.customeraccounts.dto.VirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VirtualAccountCustomerMapper {
    VirtualAccountRequestDto entityToDto(VirtualAccount account);

    VirtualAccount dtoToEntity(VirtualAccountRequestDto virtualAccountRequestDto);
}
