package com.keelean.accountmanager.exception;

import com.keelean.accountmanager.dto.BaseRestResponse;

public class RestServiceException extends RuntimeException {
    private static final long serialVersionUID = -6058067821158675893L;
    private Object[] args;
    private BaseRestResponse response;

    public RestServiceException(String code, Object... args) {
        super(code);
        this.args = args;
    }

    public RestServiceException(String code, BaseRestResponse response, Object... args) {
        super(code);
        this.args = args;
        this.response = response;
    }

    public RestServiceException(String code) {
        super(code);
    }

    public Object[] getArgs() {
        return this.args;
    }

    public BaseRestResponse getResponse() {
        return this.response;
    }
}
