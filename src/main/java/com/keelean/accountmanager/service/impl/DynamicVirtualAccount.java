package com.keelean.accountmanager.service.impl;

//import com.example.platform.dao.EntitySessionManager;

/*
@Slf4j
@Component("dynamic")
public class DynamicVirtualAccount extends AbstractVirtualAccount {

    private static long TIMEOUT = 1440;

    @Autowired
    private EntitySessionManager entitySessionManager;

    @Override
    public FullAccountResponseDto singleFullCreation(Account virtualAccount) {

        DynamicAccount dynamicAccount = (DynamicAccount) virtualAccount;
        AccountCustomer virtualAccountCustomer = composeVirtualAccount(dynamicAccount);
        entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("customize(VirtualAccount virtualAccount)");
        return FullAccountResponseDto.builder()
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .accountId(virtualAccount.getAccountId())
                .build();
    }

    private AccountCustomer composeVirtualAccount(DynamicAccount virtualAccount) {
        AccountCustomer virtualAccountCustomer = AccountCustomer.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .expiryDate(LocalDateTime.now().plus(90, ChronoUnit.DAYS))
                .status(AccountStatus.CREATED)
                .meta(AccountMeta.builder()
                        .accountName(virtualAccount.getAccountName())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .waitStartTime(LocalDateTime.now().plus(timeout, ChronoUnit.HOURS))
                        .accountType(virtualAccount.getMode())
                        .amount(virtualAccount.getAmount())
                        .build())
                .mode(AccountMode.NORMAL)
                .partnerId(virtualAccount.getPartnerId())
                .build();

        virtualAccount

        return virtualAccountCustomer;
    }


    @Override
    public List<BaseAccountResponseDto> preCreationBulkMode(List<BaseAccountRequestDto> requestDtos) {
        return super.preCreationBulkMode(requestDtos);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    @Override
    public BaseAccountResponseDto preCreation(Account virtualAccount) {
        AccountCustomer virtualAccountCustomer = AccountCustomer.builder()
                .meta(AccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        .accountName(virtualAccount.getAccountName())
                        .accountType(virtualAccount.getMode())
                        .waitStartTime(virtualAccount.getWaitStartTime())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .accountType(VirtualAccountMode.DYNAMIC)
                        .build())
                .partnerId(virtualAccount.getPartnerId())
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .status(AccountStatus.WAITING)
                .mode(AccountMode.NORMAL)
                .build();
        virtualAccountCustomer = entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("VirtualAccount4::{}", virtualAccountCustomer);
        BaseAccountResponseDto baseVirtualAccountResponseDto = BaseAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .build();

        return baseVirtualAccountResponseDto;
    }

    public List<BaseAccountResponseDto> preCreation(List<Account> virtualAccounts) {
        List<CompletableFuture<BaseAccountResponseDto>> virtualAccountFutures = new ArrayList<>();
        for (Account request : virtualAccounts) {
            CompletableFuture<BaseAccountResponseDto> virtualAccountFuture = CompletableFuture.supplyAsync(
                    () -> preCreation(request));
            virtualAccountFutures.add(virtualAccountFuture);
        }
        CompletableFuture.allOf(virtualAccountFutures.toArray(new CompletableFuture[4]))
                .exceptionally(ex -> null).join();

        return virtualAccountFutures.stream()
                .filter(virtualAccountFuture -> !virtualAccountFuture.isCompletedExceptionally())
                .map(campaignFuture -> campaignFuture.getNow(BaseAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
    }
}
 */
