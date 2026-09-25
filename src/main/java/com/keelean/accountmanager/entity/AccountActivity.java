package com.keelean.accountmanager.entity;

import lombok.*;

import jakarta.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
@Entity
public class AccountActivity extends AbstractBaseAuditableEntity {
    private String accountId;
}
