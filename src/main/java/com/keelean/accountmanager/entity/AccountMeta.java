package com.keelean.accountmanager.entity;


import com.keelean.accountmanager.enums.AccountType;
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
public class AccountMeta {

    private String accountName;
    private BigDecimal amount;
    private AccountType accountType; // change this enum here to reflect correctly
    private LocalDateTime waitStartTime;
    private Integer minMultiplier;
    private Integer maxMultiplier;
    private LocalDateTime nextEditWaitEndTime; //TODO: add this to indicate after an edit, there must be no edit until set time has passed
    private String invoicePaymentRef; //TODO: store invoice payment ref for static accounts
}