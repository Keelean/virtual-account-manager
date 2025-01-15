package com.keelean.legacy.customeraccounts.exception;

import com.example.platform.dto.BaseRestResponse;
import com.example.platform.exception.RestServiceException;

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
