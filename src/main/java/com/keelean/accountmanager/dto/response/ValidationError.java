package com.keelean.accountmanager.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ValidationError {

    private static final long serialVersionUID = -5432214354081137943L;

    private String path;

    private String code;

    @JsonIgnore
    private Object referenceValue;

    private String error;

    private String msg;
}
