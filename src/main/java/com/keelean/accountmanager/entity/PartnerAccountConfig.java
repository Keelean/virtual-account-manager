package com.keelean.accountmanager.entity;


import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.ConfigStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
@ToString
public class PartnerAccountConfig extends AbstractBaseAuditableEntity {

    private String partnerId;
    private String code;
    private String prefix;
    @Enumerated(EnumType.STRING)
    private ConfigStatus status;
    @Enumerated(EnumType.STRING)
    private AccountCapacity capacity;
    private Integer currentSequence;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private PartnerAccountConfigMeta meta;

    private Integer getMaximumSequence() {
        return Integer.parseInt("9".repeat(getCapacity().getReusableDigits()));
    }

    private boolean isNextSequenceAvailable() {
        int newSequence = this.currentSequence;
        return ++newSequence < getMaximumSequence();
    }

    public synchronized int generateSequence() {
        if (!isSharedPool()) {
            if (isNextSequenceAvailable()) {
                return ++this.currentSequence;
            } else {
                throw new IllegalArgumentException("Maximum sequence reached");
            }
        }
        throw new IllegalArgumentException("Partners cannot generate sequence for shared pool.");
    }

    public boolean isSharedPool() {
        return capacity == AccountCapacity.SHARED_POOL_1 || capacity == AccountCapacity.SHARED_POOL_10;
    }

    public boolean isMaximumSequenceReached() {
        return Objects.equals(currentSequence, getMaximumSequence());
    }
}
