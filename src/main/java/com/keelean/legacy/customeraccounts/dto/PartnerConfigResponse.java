package com.keelean.legacy.customeraccounts.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class PartnerConfigResponse {
    private String partnerId;
    private String partnerCode;
    private String prefix;
    private String capacity;
    private String mode;
    private Integer maxDeposit;
    private Integer minDeposit;
    private boolean exactPayment;
    private String partnerName;
    private Long configId;
}
