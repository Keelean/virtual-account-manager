package com.keelean.legacy.customeraccounts.entity;

import com.keelean.legacy.customeraccounts.enums.AccountCapacity;
import com.keelean.legacy.customeraccounts.enums.State;
import com.example.platform.model.base.AbstractBaseAuditableEntity;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Version;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
public class VirtualAccountPool extends AbstractBaseAuditableEntity {

    @Enumerated(value = EnumType.STRING)
    private AccountCapacity capacity;

    private Integer prefixSeries; //should only contain 2 digits, 50, 51, etc
    private Integer prefixEndSeries;
    private Integer startPrefix;
    private Integer currentSequence;
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private State state = State.OPEN;
}