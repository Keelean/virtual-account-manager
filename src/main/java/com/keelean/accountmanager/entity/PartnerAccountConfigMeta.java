package com.keelean.accountmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.keelean.accountmanager.enums.AccountType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"defaultLookupDisplayName", "gradeCode", "walletNickname", "routeId", "templateName", "mode"})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class PartnerAccountConfigMeta {
    private String defaultLookupDisplayName;
    private String gradeCode;
    private String accountDetails;
    private String routeId;
    private String templateName;
    private AccountType accountType;
    private Integer minMultiplier;
    private Integer maxMultiplier;
    private boolean exactPayment;
    private String settlementProductCode; //TODO: This should be an enum to reflect product code as on properties: va-partner.settlement.product-code,
    private Integer coolDownPeriod; //store per partner cooldown period
}