package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.TxnStatus;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("DYNAMIC")
@SuperBuilder
@ToString
public class DynamicAccount extends Account {

    private static final long DEFAULT_TIME_TO_EXPIRE = 24;

    @Override
    public boolean isExpired() {
        return getCreatedDate().plusDays(DEFAULT_TIME_TO_EXPIRE).isEqual(LocalDateTime.now());
    }

    @Override
    public boolean isAmountValid(Long amount) {
        return checkAmountValidityForDynamic(amount);
    }

    private boolean checkAmountValidityForDynamic(Long amount) {
        return Objects.equals(getAmount(), amount);
    }

    public boolean isAccountReusable() {
        //TODO:: Take into consideration that this would be different based on whether it shared or dedicated pool
        return getTxnStatus() == TxnStatus.PROCESSED && LocalDateTime.now().isAfter(getLastModifiedDate().plusDays(90));
        //getLastModifiedDate().plusDays(90).isAfter(LocalDateTime.now());

    }

    public boolean isPaymentWindowValid() {
        if (getWaitStartTime() > 0) {
            LocalDateTime localDateTime = getCreatedDate().plusHours(getWaitStartTime());
            return getTxnStatus() == TxnStatus.UNPROCESSED && localDateTime.isBefore(LocalDateTime.now());
        }
        return getTxnStatus() == TxnStatus.UNPROCESSED &&
                getCreatedDate().plusDays(DEFAULT_TIME_TO_EXPIRE).isBefore(LocalDateTime.now());
    }
}
