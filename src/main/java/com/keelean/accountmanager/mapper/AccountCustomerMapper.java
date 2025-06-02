package com.keelean.accountmanager.mapper;


import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountCustomerMapper {
    AccountRequestDto entityToDto(Account account);

    Account dtoToEntity(AccountRequestDto virtualAccountRequestDto);
}
