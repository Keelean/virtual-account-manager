package com.keelean.accountmanager.utils;

import com.google.gson.Gson;
import com.keelean.accountmanager.dto.BaseRestResponse;
import com.keelean.accountmanager.dto.PageableResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;

import java.util.List;

@Slf4j
public class AppUtils {

    /**
     * Object to json.
     *
     * @param object the object
     * @return the string
     */
    public static String objectToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static String formatStartSequence(int start, int prefixSeries, int allocated) {
        if (start == 0) return String.format("%d", prefixSeries);
        String format = "%d%0" + start + "d";
        String startSequence = String.format(format, prefixSeries, allocated);
        log.info("Format start sequence::{}", startSequence);
        return startSequence;
    }

    public static String formatEndSequence(int reusableDigits, int sequence) {
        String format = "%0" + reusableDigits + "d";
        String endSequence = String.format(format, sequence);
        log.info("Format end sequence::{}", endSequence);
        return endSequence;
    }

    public static void setSuccessResponse(BaseRestResponse response) {
        response.setSuccess(true);
        response.setCode("200");
        response.setMsg("Success");
    }

    public static void setSuccessResponseForOfflineBiller(BaseRestResponse response) {
        response.setSuccess(true);
        response.setCode("200");
        response.setMsg("Success");
    }

    public static void setSuccessResponse(PageableResponse response, Page<?> data) {
        setSuccessResponse(response, data, data.getContent());
    }

    public static void setSuccessResponse(PageableResponse response, Page<?> data, List<?> content) {
        setSuccessResponse(response);
        response.setContent(content);
        response.setNumberOfElements(data.getNumberOfElements());
        response.setSize(data.getSize());
        response.setTotalElements((int)data.getTotalElements());
        response.setTotalPages(data.getTotalPages());
        response.setNumber(data.getNumber());
        response.setEmpty(data.isEmpty());
        response.setFirst(data.isFirst());
        response.setLast(data.isLast());
        response.setSorted(data.getSort().isSorted());
    }

    public static void setSuccessResponse(BaseRestResponse response, String message, @Nullable String code) {
        response.setSuccess(true);
        response.setMsg(message);
        response.setCode(code);
    }

}
