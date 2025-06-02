package com.keelean.accountmanager.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AccountUpdateRequestDto implements Serializable {

    private String accountName;
    private BigDecimal amount;
    private Integer timeoutInMins;
    private String invoiceRef;
}
