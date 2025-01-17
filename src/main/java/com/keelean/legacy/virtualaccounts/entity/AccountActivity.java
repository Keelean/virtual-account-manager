package com.keelean.legacy.customeraccounts.entity;

import com.example.platform.model.base.AbstractBaseAuditableEntity;
import lombok.*;

import javax.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
@Entity
public class VirtualAccountActivity extends AbstractBaseAuditableEntity {
    private String accountId;
}
