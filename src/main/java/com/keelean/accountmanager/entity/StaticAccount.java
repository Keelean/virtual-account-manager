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
    public boolean isAmountValid(Long amount) {
        return checkAmountValidityForStatic(amount);
    }

    private boolean checkAmountValidityForStatic(Long amount) {
        if (getMinDeposit() == 0 || getMaxDeposit() == 0)
            return Objects.equals(getAmount(), amount);

        return amount >= getMinDeposit() || amount <= getMaxDeposit();
    }

    public boolean isWithinEditableWindow(){
        return getLastModifiedDate().plusHours(1).isAfter(LocalDateTime.now());
    }

}
