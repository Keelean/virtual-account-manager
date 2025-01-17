package com.keelean.legacy.customeraccounts.entity;


import com.keelean.legacy.customeraccounts.enums.AccountMode;
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
public class VirtualAccountMeta {

    private String accountName;
    private BigDecimal amount;
    private AccountMode mode; // change this enum here to reflect correctly
    private LocalDateTime waitStartTime;
    private Integer minMultiplier;
    private Integer maxMultiplier;
    private LocalDateTime nextEditWaitEndTime; //TODO: add this to indicate after an edit, there must be no edit until set time has passed
    private String invoicePaymentRef; //TODO: store invoice payment ref for static accounts
}