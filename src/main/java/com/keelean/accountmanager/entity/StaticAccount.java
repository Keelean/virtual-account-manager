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
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("STATIC")
@SuperBuilder
@ToString
public class StaticAccount extends Account {

    @Override
    public boolean isExpired() {
        //TODO:: Take into consideration that this would be different based on whether it shared or dedicated pool
        return getTxnStatus() == TxnStatus.PROCESSED && LocalDateTime.now().isAfter(getLastModifiedDate().plusDays(180));
        //getLastModifiedDate().plusDays(90).isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isAmountValid(BigDecimal amount) {
        return checkAmountValidityForStatic(amount);
    }

    private boolean checkAmountValidityForStatic(BigDecimal amount) {
        if (amount == null || getAmount() == null)
            return false;

        if (!isSet(getMinDeposit()) || !isSet(getMaxDeposit()))
            return getAmount().compareTo(amount) == 0;

        // Min and max deposits are multipliers in tenths of the account amount: 5 allows 0.5x, 20 allows 2x
        BigDecimal lowest = getAmount().multiply(BigDecimal.valueOf(getMinDeposit())).divide(BigDecimal.TEN);
        BigDecimal highest = getAmount().multiply(BigDecimal.valueOf(getMaxDeposit())).divide(BigDecimal.TEN);
        return amount.compareTo(lowest) >= 0 && amount.compareTo(highest) <= 0;
    }

    private static boolean isSet(Long deposit) {
        return deposit != null && deposit > 0;
    }

    public boolean isWithinEditableWindow(){
        return getLastModifiedDate().plusHours(1).isAfter(LocalDateTime.now());
    }

}
