package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    private Integer startPrefixCount;
    private Integer currentSequence;
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private State state = State.OPEN;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Set<String> excludedPrefixStart;
    private Integer allocationCount;

    public boolean isOpen() {
        return state == State.OPEN;
    }

    public boolean isClosed() {
        return state == State.CLOSED;
    }


    public boolean isSharedPool() {
        return capacity == AccountCapacity.SHARED_POOL_1 || capacity == AccountCapacity.SHARED_POOL_10;
    }

    //generate sequence for shared pool
    public synchronized int generateSequence() {
        if (isSharedPool()) {
            if (isNextSequenceAvailable() && isOpen()) {
                return ++this.currentSequence;
            } else {
                throw new IllegalArgumentException("Maximum sequence reached");
            }
        }
        throw new IllegalArgumentException("Shared pool cannot generate sequence for dedicated pool.");
    }

    private boolean isNextSequenceAvailable() {
        int newSequence = this.currentSequence;
        return ++newSequence < getMaximumSequence();
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