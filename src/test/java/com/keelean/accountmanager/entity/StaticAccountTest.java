package com.keelean.accountmanager.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticAccountTest {

    private static StaticAccount account(String amount, long minDeposit, long maxDeposit) {
        return StaticAccount.builder()
                .amount(new BigDecimal(amount))
                .minDeposit(minDeposit)
                .maxDeposit(maxDeposit)
                .build();
    }

    @Test
    void withoutDepositRangeOnlyTheExactAmountIsValid() {
        StaticAccount account = account("1000", 0, 0);

        assertTrue(account.isAmountValid(new BigDecimal("1000.00")));
        assertFalse(account.isAmountValid(new BigDecimal("999.99")));
        assertFalse(account.isAmountValid(new BigDecimal("1000.01")));
    }

    @Test
    void depositRangeIsTheAmountScaledByTheMultipliersInTenths() {
        // 5 -> 0.5x, 20 -> 2x of 1000
        StaticAccount account = account("1000", 5, 20);

        assertTrue(account.isAmountValid(new BigDecimal("500")));
        assertTrue(account.isAmountValid(new BigDecimal("1250.50")));
        assertTrue(account.isAmountValid(new BigDecimal("2000")));
        assertFalse(account.isAmountValid(new BigDecimal("499.99")));
        assertFalse(account.isAmountValid(new BigDecimal("2000.01")));
    }

    @Test
    void rangeKeepsDecimalAmounts() {
        // 1 -> 0.1x, 10 -> 1x of 99.95
        StaticAccount account = account("99.95", 1, 10);

        assertTrue(account.isAmountValid(new BigDecimal("9.995")));
        assertTrue(account.isAmountValid(new BigDecimal("99.95")));
        assertFalse(account.isAmountValid(new BigDecimal("9.99")));
        assertFalse(account.isAmountValid(new BigDecimal("99.96")));
    }

    @Test
    void missingAmountsAreInvalid() {
        assertFalse(account("1000", 5, 20).isAmountValid(null));
        assertFalse(StaticAccount.builder().minDeposit(5L).maxDeposit(20L).build().isAmountValid(new BigDecimal("1000")));
    }
}
