package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
@ToString
public class AccountCustomer extends AbstractBaseAuditableEntity {

    private String accountId;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private String partnerId;
    private Long partnerConfigId; // reference to VirtualAccountPartnerConfig -> id
    private String referenceId;
    private LocalDateTime expiryDate;
    @Enumerated(EnumType.STRING)
    private AccountMode mode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private AccountMeta meta;
}
