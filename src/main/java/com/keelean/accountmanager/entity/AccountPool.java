package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
public class AccountPool extends AbstractBaseAuditableEntity {

    @Enumerated(value = EnumType.STRING)
    private AccountCapacity capacity;

    private Integer prefixSeries; //should only contain 2 digits, 50, 51, etc
    private Integer prefixEndSeries;
    private Integer startPrefix;
    @Builder.Default
    private Integer startPrefixCount = 0;
    private Integer currentSequence;
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private State state = State.OPEN;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Set<String> excludedPrefixStart = new HashSet<>();
    @Builder.Default
    private Integer allocationCount = 0;

    public boolean isOpen() {
        return state == State.OPEN;
    }

    public boolean isClosed() {
        return state == State.CLOSED;
    }


    public boolean isSharedPool() {
        return capacity == AccountCapacity.SHARED_POOL_1 || capacity == AccountCapacity.SHARED_POOL_10;
    }

    // Reserves count consecutive shared-pool sequences and returns the first; the caller must hold the row lock
    public int reserveSequences(int count) {
        if (!isSharedPool()) {
            throw new IllegalArgumentException("Shared pool cannot generate sequence for dedicated pool.");
        }
        if (this.currentSequence + count >= getMaximumSequence() || !isOpen()) {
            throw new IllegalArgumentException("Maximum sequence reached");
        }
        int first = this.currentSequence + 1;
        this.currentSequence += count;
        return first;
    }

    private Integer getMaximumSequence() {
        return Integer.parseInt("9".repeat(getCapacity().getReusableDigits()));
    }

    private boolean isMaximumSequenceReached() {
        return Objects.equals(currentSequence, getMaximumSequence());
    }

    public boolean isSharedPoolAllocationFull() {
        int newAllocation = this.startPrefixCount;
        return ++newAllocation > Math.pow(10.0, AccountCapacity.getStartPrefixWidth(this.capacity));
    }

}