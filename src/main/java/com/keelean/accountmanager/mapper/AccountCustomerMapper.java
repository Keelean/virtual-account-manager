package com.keelean.accountmanager.mapper;


import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.DynamicAccount;
import com.keelean.accountmanager.entity.StaticAccount;
import com.keelean.accountmanager.enums.AccountType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountCustomerMapper {
    AccountRequestDto entityToDto(Account account);

    StaticAccount dtoToStaticEntity(AccountRequestDto virtualAccountRequestDto);

    DynamicAccount dtoToDynamicEntity(AccountRequestDto virtualAccountRequestDto);

    default Account dtoToEntity(AccountRequestDto virtualAccountRequestDto) {
        if (virtualAccountRequestDto == null) {
            return null;
        }
        return virtualAccountRequestDto.getAccountType() == AccountType.STATIC
                ? dtoToStaticEntity(virtualAccountRequestDto)
                : dtoToDynamicEntity(virtualAccountRequestDto);
    }
}
