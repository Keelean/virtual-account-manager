package com.keelean.accountmanager.exception;


import com.keelean.accountmanager.dto.BaseRestResponse;

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
