package com.keelean.legacy.customeraccounts.mapper;

import com.example.platform.core.pojo.base.BaseRestResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BaseRestResponseMapper {
    BaseRestResponse technicalDtoToCorePojo(com.Example.africa.technical.dto.BaseRestResponse baseRestResponse);
}
