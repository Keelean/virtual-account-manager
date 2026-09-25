package com.keelean.accountmanager.entity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractBaseAuditableEntity extends AbstractVersionedAuditable<String, Long> {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;
}
