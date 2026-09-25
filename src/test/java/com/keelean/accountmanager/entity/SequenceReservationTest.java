package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// THOUSAND_10 has 4 reusable digits, so its highest usable sequence is 9998 (9999 is the maximum)
class SequenceReservationTest {

    private static PartnerAccountConfig dedicatedConfig(int currentSequence) {
        return PartnerAccountConfig.builder().capacity(AccountCapacity.THOUSAND_10).currentSequence(currentSequence).build();
    }

    private static AccountPool sharedPool(int currentSequence, State state) {
        return AccountPool.builder().capacity(AccountCapacity.SHARED_POOL_10).currentSequence(currentSequence).state(state).build();
    }

    @Test
    void configReservesConsecutiveSequencesFromTheNextOne() {
        PartnerAccountConfig config = dedicatedConfig(-1);

        assertEquals(0, config.reserveSequences(5));
        assertEquals(4, config.getCurrentSequence());
        assertEquals(5, config.reserveSequences(1));
        assertEquals(5, config.getCurrentSequence());
    }

    @Test
    void configReservesUpToTheLastUsableSequence() {
        PartnerAccountConfig config = dedicatedConfig(9990);

        assertEquals(9991, config.reserveSequences(8));
        assertEquals(9998, config.getCurrentSequence());
    }

    @Test
    void configRejectsAReservationPastTheMaximumWithoutChangingTheSequence() {
        PartnerAccountConfig config = dedicatedConfig(9990);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> config.reserveSequences(9));
        assertEquals("Maximum sequence reached", ex.getMessage());
        assertEquals(9990, config.getCurrentSequence());
    }

    @Test
    void configInASharedPoolCannotReserve() {
        PartnerAccountConfig config = PartnerAccountConfig.builder().capacity(AccountCapacity.SHARED_POOL_10).currentSequence(0).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> config.reserveSequences(1));
        assertEquals("Partners cannot generate sequence for shared pool.", ex.getMessage());
    }

    @Test
    void sharedPoolReservesConsecutiveSequences() {
        AccountPool pool = sharedPool(9, State.OPEN);

        assertEquals(10, pool.reserveSequences(3));
        assertEquals(12, pool.getCurrentSequence());
    }

    @Test
    void closedSharedPoolRejectsReservations() {
        AccountPool pool = sharedPool(9, State.CLOSED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> pool.reserveSequences(1));
        assertEquals("Maximum sequence reached", ex.getMessage());
        assertEquals(9, pool.getCurrentSequence());
    }

    @Test
    void dedicatedPoolCannotReserveSharedSequences() {
        AccountPool pool = AccountPool.builder().capacity(AccountCapacity.THOUSAND_10).currentSequence(0).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> pool.reserveSequences(1));
        assertEquals("Shared pool cannot generate sequence for dedicated pool.", ex.getMessage());
    }
}
