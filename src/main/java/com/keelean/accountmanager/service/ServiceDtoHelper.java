package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.BaseRestResponse;

public class ServiceDtoHelper {
    public static BaseRestResponse getBaseResponseObj(String statusCode, String errorMessage) {
        BaseRestResponse baseRestResponse = new BaseRestResponse();
        baseRestResponse.setCode(statusCode);
        baseRestResponse.setMsg(errorMessage);
        baseRestResponse.setSuccess(false);
        return baseRestResponse;
    }
}
