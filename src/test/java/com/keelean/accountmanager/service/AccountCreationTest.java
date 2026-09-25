package com.keelean.accountmanager.service;

import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.entity.PartnerAccountConfigMeta;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import com.keelean.accountmanager.service.impl.DynamicVirtualAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCreationTest {

    @Mock
    private PartnerAccountConfigService configService;
    @Mock
    private AccountCustomerRepo customerRepository;
    @Mock
    private SequenceAllocator sequenceAllocator;

    @InjectMocks
    private DynamicVirtualAccount dynamicVirtualAccount;

    private PartnerAccountConfig config;

    @BeforeEach
    void setUp() {
        config = PartnerAccountConfig.builder()
                .partnerId("P1")
                .prefix("310000")
                .capacity(AccountCapacity.THOUSAND_10)
                .meta(PartnerAccountConfigMeta.builder().defaultLookupDisplayName("ACME").accountType(AccountType.DYNAMIC).build())
                .build();
        config.setId(7L);
    }

    private static BaseAccountRequestDto request(String referenceId, String accountId) {
        return BaseAccountRequestDto.builder()
                .partnerId("P1")
                .referenceId(referenceId)
                .accountId(accountId)
                .accountType(AccountType.DYNAMIC)
                .build();
    }

    @Test
    void reservesAllIdsForOnePartnerInOneCallAndKeepsRequestOrder() {
        when(customerRepository.findByReferenceIdInOrAccountIdIn(anyCollection(), anyCollection())).thenReturn(Collections.emptyList());
        when(configService.getPartnerAccountConfig(AccountType.DYNAMIC, "P1")).thenReturn(config);
        when(sequenceAllocator.reserveAccountIds(config, 3)).thenReturn(List.of("3100000000", "3100000001", "3100000002"));

        List<Account> accounts = dynamicVirtualAccount.createVirtualAccounts(List.of(request("R1", null), request("R2", null), request("R3", null)));

        assertEquals(List.of("R1", "R2", "R3"), accounts.stream().map(Account::getReferenceId).toList());
        assertEquals(List.of("3100000000", "3100000001", "3100000002"), accounts.stream().map(Account::getAccountId).toList());
        verify(sequenceAllocator).reserveAccountIds(config, 3);
    }

    @Test
    void partnerSuppliedAccountIdsDoNotConsumeSequences() {
        when(customerRepository.findByReferenceIdInOrAccountIdIn(anyCollection(), anyCollection())).thenReturn(Collections.emptyList());
        when(configService.getPartnerAccountConfig(AccountType.DYNAMIC, "P1")).thenReturn(config);
        when(sequenceAllocator.reserveAccountIds(config, 1)).thenReturn(List.of("3100000000"));

        List<Account> accounts = dynamicVirtualAccount.createVirtualAccounts(List.of(request("R1", "3100000123"), request("R2", null)));

        assertEquals(List.of("3100000123", "3100000000"), accounts.stream().map(Account::getAccountId).toList());
        verify(sequenceAllocator).reserveAccountIds(config, 1);
    }

    @Test
    void existingReferenceFailsBeforeAnySequenceIsReserved() {
        AccountCustomer existing = AccountCustomer.builder().referenceId("R2").accountId("3100000009").build();
        when(customerRepository.findByReferenceIdInOrAccountIdIn(anyCollection(), anyCollection())).thenReturn(List.of(existing));

        RestServiceException ex = assertThrows(RestServiceException.class,
                () -> dynamicVirtualAccount.createVirtualAccounts(List.of(request("R1", null), request("R2", null))));

        assertEquals(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), ex.getMessage());
        verify(sequenceAllocator, never()).reserveAccountIds(any(), anyInt());
    }

    @Test
    void invalidSuppliedAccountIdFailsBeforeAnySequenceIsReserved() {
        when(customerRepository.findByReferenceIdInOrAccountIdIn(anyCollection(), anyCollection())).thenReturn(Collections.emptyList());
        when(configService.getPartnerAccountConfig(AccountType.DYNAMIC, "P1")).thenReturn(config);

        RestServiceException ex = assertThrows(RestServiceException.class,
                () -> dynamicVirtualAccount.createVirtualAccounts(List.of(request("R1", null), request("R2", "9900000001"))));

        assertEquals(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode(), ex.getMessage());
        verify(sequenceAllocator, never()).reserveAccountIds(any(), anyInt());
    }
}
