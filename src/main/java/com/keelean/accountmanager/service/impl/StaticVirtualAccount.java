package com.keelean.accountmanager.service.impl;

/*
@Component("static")
@Slf4j
public class StaticVirtualAccount extends AbstractVirtualAccount {

    @Autowired
    private EntitySessionManager entitySessionManager;
    @Override
    public FullAccountResponseDto singleFullCreation(Account virtualAccount) {
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

        AccountCustomer virtualAccountCustomer = AccountCustomer.builder()
                .meta(AccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .waitStartTime(virtualAccount.getWaitStartTime())
                        .accountName(virtualAccount.getAccountName())
                        .accountType(virtualAccount.getMode())
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
        return FullAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .build();
    }
}

 */
