package com.keelean.accountmanager.mapper;

import com.keelean.accountmanager.dto.BaseRestResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BaseRestResponseMapper {
    BaseRestResponse technicalDtoToCorePojo(BaseRestResponse baseRestResponse);
}
