package com.keelean.accountmanager.service;

import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCustomerServiceTest {

    private static final String UNKNOWN_ID = "doesnotexist";

    @Mock
    private AccountCustomerRepo repo;

    @InjectMocks
    private AccountCustomerService service;

    @Test
    void getVirtualAccountFromListRejectsUnknownAccount() {
        when(repo.findByReferenceIdOrAccountId(UNKNOWN_ID, UNKNOWN_ID)).thenReturn(Collections.emptyList());

        RestServiceException ex = assertThrows(RestServiceException.class,
                () -> service.getVirtualAccountFromList(UNKNOWN_ID));

        assertEquals(ErrorCodes.ACCOUNT_DOES_NOT_EXIST.getCode(), ex.getMessage());
        assertArrayEquals(new Object[]{UNKNOWN_ID}, ex.getArgs());
    }

    @Test
    void findVirtualAccountAndValidateAmountRejectsUnknownAccount() {
        when(repo.findByReferenceIdOrAccountId(UNKNOWN_ID, UNKNOWN_ID)).thenReturn(Collections.emptyList());

        RestServiceException ex = assertThrows(RestServiceException.class,
                () -> service.findVirtualAccountAndValidateAmount(UNKNOWN_ID, BigDecimal.TEN));

        assertEquals(ErrorCodes.ACCOUNT_DOES_NOT_EXIST.getCode(), ex.getMessage());
    }
}
