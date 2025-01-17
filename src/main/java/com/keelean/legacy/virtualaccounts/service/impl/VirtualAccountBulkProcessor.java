package com.keelean.legacy.customeraccounts.service.impl;

//@Service
public class VirtualAccountBulkProcessor {

    /*
    @Autowired
    @Qualifier("virtualAccountTaskExecutor")
    private Executor virtualAccountExecutor;

    @Autowired
    private VirtualAccountFactoryProvider provider;


    public List<BaseVirtualAccountResponseDto> processParallelyWithExecutorService(List<BaseVirtualAccountRequestDto> virtualAccounts) throws InterruptedException {
        List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        for (BaseVirtualAccountRequestDto dto : virtualAccounts) {
            CompletableFuture<BaseVirtualAccountResponseDto> response = CompletableFuture.supplyAsync(
                    ()-> {
                        VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(dto);
                        if(virtualAccount.getTimeoutInMins() != null){
                            return abstractVirtualAccount.singleFullCreation(virtualAccount);
                        }else {
                           return abstractVirtualAccount.preCreation(virtualAccount);
                        }
                    }, virtualAccountExecutor);
            futures.add(response);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null).join();

        List<BaseVirtualAccountResponseDto> collect = futures.stream()
                .filter(b -> b != null)
                .map(b -> b.getNow(BaseVirtualAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
        return collect;
    }*/
}
