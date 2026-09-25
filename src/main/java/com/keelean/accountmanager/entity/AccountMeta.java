package com.keelean.accountmanager.entity;


import com.keelean.accountmanager.enums.AccountType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
// Stored as jsonb; Hypersistence Utils deep-copies JSON attributes via Java serialization
public class AccountMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String accountName;
    private BigDecimal amount;
    private AccountType accountType; // change this enum here to reflect correctly
    private LocalDateTime waitStartTime;
    private Integer minMultiplier;
    private Integer maxMultiplier;
    private LocalDateTime nextEditWaitEndTime; //TODO: add this to indicate after an edit, there must be no edit until set time has passed
    private String invoicePaymentRef; //TODO: store invoice payment ref for static accounts
}