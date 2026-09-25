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
        if (getMinDeposit() == 0 || getMaxDeposit() == 0)
            return getAmount() != null && amount != null && getAmount().compareTo(amount) == 0;

        return amount.compareTo(BigDecimal.valueOf(getMinDeposit())) >= 0 || amount.compareTo(BigDecimal.valueOf(getMaxDeposit())) <= 0;
    }

    public boolean isWithinEditableWindow(){
        return getLastModifiedDate().plusHours(1).isAfter(LocalDateTime.now());
    }

}
