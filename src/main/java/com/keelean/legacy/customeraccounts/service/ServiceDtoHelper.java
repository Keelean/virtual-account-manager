package com.keelean.legacy.customeraccounts.service;

import com.example.platform.dto.BaseRestResponse;

public class ServiceDtoHelper {
    public static BaseRestResponse getBaseResponseObj(String statusCode, String errorMessage) {
        BaseRestResponse baseRestResponse = new BaseRestResponse();
        baseRestResponse.setCode(statusCode);
        baseRestResponse.setMsg(errorMessage);
        baseRestResponse.setSuccess(false);
        return baseRestResponse;
    }
}
