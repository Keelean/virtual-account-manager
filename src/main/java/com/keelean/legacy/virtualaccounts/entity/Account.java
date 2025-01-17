package com.keelean.legacy.customeraccounts.entity;

import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class VirtualAccount {
    private String accountName;
    private BigDecimal amount;
    private VirtualAccountMode mode;
    private Integer timeoutInMins;
    private LocalDateTime waitStartTime;
    private String partnerId;
    private String accountId;
    private String referenceId;
    private String invoiceRef;
    private Integer minDeposit;
    private Integer maxDeposit;
}
