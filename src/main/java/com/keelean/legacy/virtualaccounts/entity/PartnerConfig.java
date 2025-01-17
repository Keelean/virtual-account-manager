package com.keelean.legacy.customeraccounts.entity;

import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.models.auth.In;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"defaultLookupDisplayName", "gradeCode", "walletNickname", "routeId", "templateName", "mode"})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class PartnerConfig {
    private String defaultLookupDisplayName;
    private String gradeCode;
    private String accountDetails;
    private String routeId;
    private String templateName;
    private VirtualAccountMode mode;
    private Integer minMultiplier;
    private Integer maxMultiplier;
    private boolean exactPayment;
    private String settlementProductCode; //TODO: This should be an enum to reflect product code as on properties: va-partner.settlement.product-code,
    private Integer coolDownPeriod; //store per partner cooldown period
}