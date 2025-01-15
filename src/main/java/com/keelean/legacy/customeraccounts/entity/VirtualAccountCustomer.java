package com.keelean.legacy.customeraccounts.entity;

import com.keelean.legacy.customeraccounts.enums.AccountMode;
import com.keelean.legacy.customeraccounts.enums.AccountStatus;
import com.example.platform.model.base.AbstractBaseAuditableEntity;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
@ToString
public class VirtualAccountCustomer extends AbstractBaseAuditableEntity {

    private String accountId;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private String partnerId;
    private String referenceId;
    private LocalDateTime expiryDate;
    private String invoicePaymentRef;
    @Enumerated(EnumType.STRING)
    private AccountMode mode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private VirtualAccountMeta meta;
}
