package com.keelean.accountmanager.exception;


import com.keelean.accountmanager.dto.BaseRestResponse;

public class ConfigNotFoundException extends RestServiceException {
    public ConfigNotFoundException(String code, Object... args) {
        super(code, args);
    }

    public ConfigNotFoundException(String code, BaseRestResponse response, Object... args) {
        super(code, response, args);
    }

    public ConfigNotFoundException(String code) {
        super(code);
    }
}
