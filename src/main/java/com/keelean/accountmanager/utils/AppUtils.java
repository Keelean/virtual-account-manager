package com.keelean.accountmanager.utils;

import com.keelean.accountmanager.dto.BaseRestResponse;
import com.keelean.accountmanager.dto.PageableResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.util.List;

@Slf4j
public class AppUtils {

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

}
