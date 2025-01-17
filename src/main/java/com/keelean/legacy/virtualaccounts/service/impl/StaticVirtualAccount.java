package com.keelean.legacy.customeraccounts.service.impl;

import com.keelean.legacy.customeraccounts.dto.FullVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountCustomer;
import com.keelean.legacy.customeraccounts.enums.AccountMode;
import com.keelean.legacy.customeraccounts.enums.AccountStatus;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.keelean.legacy.customeraccounts.service.AbstractVirtualAccount;
import com.example.platform.dao.EntitySessionManager;
import com.example.platform.exception.RestServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component("static")
@Slf4j
public class StaticVirtualAccount extends AbstractVirtualAccount {

    @Autowired
    private EntitySessionManager entitySessionManager;
    @Override
    public FullVirtualAccountResponseDto singleFullCreation(VirtualAccount virtualAccount) {
        AccountMode mode = AccountMode.NORMAL;
        if(Objects.nonNull(virtualAccount.getInvoiceRef())){
            mode = AccountMode.INVOICE;
        }

        Integer minDeposit = null;
        Integer maxDeposit = null;

        log.info("MULTIPLIRT[{}]", virtualAccount);

        if(virtualAccount.getMaxDeposit() != null && virtualAccount.getMinDeposit() != null){
            minDeposit = virtualAccount.getMinDeposit();
            maxDeposit = virtualAccount.getMaxDeposit();

            boolean isMinDepositValid = minDeposit >= 1 && minDeposit <= 10;
            boolean isMaxDepositValid = maxDeposit >= 10 && maxDeposit <= 100;

            if(!isMinDepositValid || !isMaxDepositValid){
                throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            }
        }

        VirtualAccountCustomer virtualAccountCustomer = VirtualAccountCustomer.builder()
                .meta(VirtualAccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .waitStartTime(virtualAccount.getWaitStartTime())
                        .accountName(virtualAccount.getAccountName())
                        .mode(virtualAccount.getMode())
                        .minMultiplier(minDeposit)
                        .maxMultiplier(maxDeposit)
                        .build())
                .expiryDate(LocalDateTime.now().plus(1, ChronoUnit.YEARS))
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .status(AccountStatus.CREATED)
                .invoicePaymentRef(virtualAccount.getInvoiceRef())
                .mode(mode)
                .partnerId(virtualAccount.getPartnerId())
                .build();
        entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        return FullVirtualAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .build();
    }
}
