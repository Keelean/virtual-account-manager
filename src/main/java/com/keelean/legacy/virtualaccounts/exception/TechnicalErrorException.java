package com.keelean.legacy.customeraccounts.exception;

import com.example.platform.dto.BaseRestResponse;
import com.example.platform.exception.RestServiceException;

public class TechnicalErrorException extends RestServiceException {

    public TechnicalErrorException(String code, Object... args) {
        super(code, args);
    }

    public TechnicalErrorException(String code, BaseRestResponse response, Object... args) {
        super(code, response, args);
    }

    public TechnicalErrorException(String code) {
        super(code);
    }
}
