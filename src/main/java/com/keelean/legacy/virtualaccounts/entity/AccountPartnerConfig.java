package com.keelean.legacy.customeraccounts.entity;

import com.keelean.legacy.customeraccounts.enums.AccountCapacity;
import com.keelean.legacy.customeraccounts.enums.AccountStatus;
import com.keelean.legacy.customeraccounts.enums.ConfigStatus;
import com.example.platform.core.pojo.transaction.PSPTransactionRequest;
import com.example.platform.model.base.AbstractBaseAuditableEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.json.simple.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
@TypeDefs({@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)})
@ToString
public class VirtualAccountPartnerConfig extends AbstractBaseAuditableEntity {

    private String partnerId;
    private String code;
    private String prefix;
    @Enumerated(EnumType.STRING)
    private ConfigStatus status;
    @Enumerated(EnumType.STRING)
    private AccountCapacity capacity;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private PartnerConfig meta;
}
