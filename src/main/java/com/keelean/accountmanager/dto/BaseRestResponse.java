package com.keelean.accountmanager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.keelean.accountmanager.dto.response.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
@Data
@JsonPropertyOrder({"st", "msgid", "msg", "devErrorMessage", "timestamp", "validationError"})
public class BaseRestResponse implements Serializable {

    private static final long serialVersionUID = 3692959428126102727L;

    @JsonProperty("st")
    private boolean success;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp = LocalDateTime.now();
    @JsonProperty("msgid")
    private String code;

    @JsonProperty("msg")
    private String msg;

    private String devErrorMessage;

    @JsonProperty("validationError")
    private Collection<ValidationError> validationError;


}
