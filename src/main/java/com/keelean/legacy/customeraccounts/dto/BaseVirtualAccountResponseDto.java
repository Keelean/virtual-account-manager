package com.keelean.legacy.customeraccounts.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseVirtualAccountResponseDto implements Serializable {

    //private String accountName;
    //private BigDecimal amount;
    //private VirtualAccountMode mode;
    //private Integer timeoutInMins;
    private String partnerCode;
    protected String accountId;
    protected String referenceId;
}
