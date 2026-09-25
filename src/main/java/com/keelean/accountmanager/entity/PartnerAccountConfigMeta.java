package com.keelean.accountmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.keelean.accountmanager.enums.AccountType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"defaultLookupDisplayName", "gradeCode", "walletNickname", "routeId", "templateName", "mode"})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
// Stored as jsonb; Hypersistence Utils deep-copies JSON attributes via Java serialization
public class PartnerAccountConfigMeta implements Serializable {

    private static final long serialVersionUID = 1L;

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