package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccountCustomer extends AbstractBaseAuditableEntity {

    private String accountId;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private Long partnerConfigId; // reference to VirtualAccountPartnerConfig -> id
    private String referenceId;
    private LocalDateTime expiryDate;
    @Enumerated(EnumType.STRING)
    private AccountMode mode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private AccountMeta meta;
}