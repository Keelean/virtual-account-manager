package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private AccountMeta meta;
}
