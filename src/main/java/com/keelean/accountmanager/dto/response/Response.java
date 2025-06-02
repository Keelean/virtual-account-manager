package com.keelean.accountmanager.dto.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.keelean.accountmanager.dto.BaseRestResponse;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

@Data
@JsonPropertyOrder({ "st", "msgid", "msg", "devErrorMessage", "timestamp", "validationError" })
public class Response<T> extends BaseRestResponse {

    private T data;

    public void setFailureResponse(String errorCode, String message, String devErrorMessage,
                                   boolean injectDevErrorMessage, Collection<ValidationError> validationErrors){
        this.setSuccess(false);
        this.setCode(errorCode);
        this.setMsg(message);
        this.setValidationError(validationErrors);
        if (injectDevErrorMessage) {
            this.setDevErrorMessage(devErrorMessage);
        }
    }

    public void setSuccessResponse(T data, String message) {
        this.setData(data);
        this.setSuccess(true);
        if (!StringUtils.isEmpty(message)) {
            this.setMsg(message);
        }
    }
}
