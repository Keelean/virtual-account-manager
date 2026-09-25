package com.keelean.accountmanager.entity;

import com.keelean.accountmanager.enums.AccountType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Hypersistence Utils deep-copies jsonb attributes via Java serialization; a non-Serializable meta fails every insert
class JsonMetaSerializableTest {

    @Test
    void accountMetaSurvivesSerialization() throws Exception {
        AccountMeta meta = AccountMeta.builder()
                .accountName("ACME Customer")
                .amount(new BigDecimal("150.25"))
                .accountType(AccountType.DYNAMIC)
                .waitStartTime(LocalDateTime.of(2026, 9, 25, 12, 0))
                .build();

        assertEquals(meta, roundTrip(meta));
    }

    @Test
    void partnerAccountConfigMetaSurvivesSerialization() throws Exception {
        PartnerAccountConfigMeta meta = PartnerAccountConfigMeta.builder()
                .defaultLookupDisplayName("ACME")
                .accountType(AccountType.STATIC)
                .minMultiplier(1)
                .maxMultiplier(10)
                .build();

        PartnerAccountConfigMeta copy = roundTrip(meta);
        assertEquals(meta.getDefaultLookupDisplayName(), copy.getDefaultLookupDisplayName());
        assertEquals(meta.getAccountType(), copy.getAccountType());
        assertEquals(meta.getMaxMultiplier(), copy.getMaxMultiplier());
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
