package com.keelean.accountmanager.service;

import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.dto.WrapperAccountDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class AccountManager {

    private static final Integer MAX_ITEM_SIZE = 100;

    @Autowired
    private AccountFactoryProvider provider;

    @Autowired
    @Qualifier("virtualAccountTaskExecutor")
    private Executor virtualAccountExecutor;

    @SneakyThrows
    public BaseAccountResponseDto createVirtualAccount(BaseAccountRequestDto requestDto, String mode){
        //Validate Request
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(mode);
        Account virtualAccount = abstractVirtualAccount.createVirtualAccount(requestDto);
        virtualAccount.setReferenceId(requestDto.getReferenceId());
        return abstractVirtualAccount.preCreation(virtualAccount);
    }

    @SneakyThrows
    public BaseAccountResponseDto createVirtualAccount(AccountRequestDto requestDto){
        //Validate Request
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(requestDto.getAccountType().name().toLowerCase());
        Account virtualAccount = abstractVirtualAccount.createVirtualAccount(requestDto);
        return abstractVirtualAccount.singleFullCreation(virtualAccount);
    }


    @SneakyThrows
    public List<BaseAccountResponseDto> createVirtualAccount(WrapperAccountDto<AccountRequestDto> requestDtos){

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<AccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, AccountRequestDto> treeMap = new HashMap<>();

        for (AccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }

        List<BaseAccountResponseDto> virtualAccounts = new ArrayList<>();

        virtualAccounts = baseVirtualAccountRequestDtos.stream()
                .map(request -> {
                    AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(request.getAccountType().name().toLowerCase());
                    Account virtualAccount = abstractVirtualAccount.createVirtualAccount(request);
                    virtualAccount.setReferenceId(request.getReferenceId());
                    return abstractVirtualAccount.singleFullCreation(virtualAccount);
                }).collect(Collectors.toList());

        log.info("SIZE::{}", virtualAccounts.size());//5200010304
        return virtualAccounts;
    }

    @SneakyThrows
    public List<BaseAccountResponseDto> processParallelyWithExecutorService(WrapperAccountDto<BaseAccountRequestDto> requestDtos) {

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<BaseAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, BaseAccountRequestDto> treeMap = new HashMap<>();

        for (BaseAccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }


        //List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        List<CompletableFuture<BaseAccountResponseDto>> futures = new ArrayList<>();

        for (BaseAccountRequestDto dto : baseVirtualAccountRequestDtos){
            CompletableFuture<BaseAccountResponseDto> response = CompletableFuture.supplyAsync(
                    ()-> {
                        Account virtualAccount = abstractVirtualAccount.createVirtualAccount(dto);
                        if(virtualAccount.getTimeoutInMins() != null){
                            return abstractVirtualAccount.singleFullCreation(virtualAccount);
                        }else {
                            return abstractVirtualAccount.preCreation(virtualAccount);
                        }
                        },
                    virtualAccountExecutor);
            futures.add(response);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .exceptionally(ex -> null).join();



        List<BaseAccountResponseDto> collect = futures.stream()
                .filter(b -> b != null)
                .map(b -> b.getNow(BaseAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
        return collect;
    }

    @SneakyThrows
    public List<BaseAccountResponseDto> processParallelyWithExecutorServicess(WrapperAccountDto<BaseAccountRequestDto> requestDtos) {

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<BaseAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, BaseAccountRequestDto> treeMap = new HashMap<>();

        for (BaseAccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }


        //List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        List<CompletableFuture<BaseAccountResponseDto>> futures = baseVirtualAccountRequestDtos.stream().map(
                response -> CompletableFuture.supplyAsync(
                        ()-> {
                            Account virtualAccount = abstractVirtualAccount.createVirtualAccount(response);
                            if(virtualAccount.getTimeoutInMins() != null){
                                return abstractVirtualAccount.singleFullCreation(virtualAccount);
                            }else {
                                return abstractVirtualAccount.preCreation(virtualAccount);
                            }
                        }, virtualAccountExecutor)
        ).collect(Collectors.toList());

        /*CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null).join();*/

        return futures.stream().map(CompletableFuture::join)
                .collect(Collectors.toList());

        /*
        List<BaseVirtualAccountResponseDto> collect = futures.stream()
                .filter(b -> b != null)
                .map(b -> b.getNow(BaseVirtualAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
        return collect;*/
    }



        @SneakyThrows
        public List<BaseAccountResponseDto> createVirtualAccountIdsPreBulkMode(WrapperAccountDto<BaseAccountRequestDto> requestDtos) {

            if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
                log.info("Items size cannot be greater than 100. You have {} items", requestDtos.getRequests().size());
                throw new RestServiceException(ErrorCodes.ACCOUNT_LENGTH_VALIDATION.getCode(), MAX_ITEM_SIZE);
            }
            Set<BaseAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

            Map<String, BaseAccountRequestDto> treeMap = new HashMap<>();

            for (BaseAccountRequestDto dto: requestDtoSet) {
                if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                    continue;
                }else{
                    treeMap.put(dto.getAccountId(), dto);
                }
            }

            List<BaseAccountRequestDto> baseVirtualAccountRequestDtos;

            if(treeMap.isEmpty()){
                baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
            }else {
                baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
            }

            List<BaseAccountResponseDto> virtualAccounts = new ArrayList<>();

            AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
            Iterable<BaseAccountRequestDto> iterable = () -> baseVirtualAccountRequestDtos.iterator();
            Stream<BaseAccountRequestDto> stream = StreamSupport.stream(iterable.spliterator(), true);
            stream.forEach(req -> {
                try {
                    req.setAccountType(AccountType.STATIC);
                    Account virtualAccount = abstractVirtualAccount.createVirtualAccount(req);
                    if(virtualAccount.getTimeoutInMins() != null){
                        virtualAccounts.add(abstractVirtualAccount.singleFullCreation(virtualAccount));
                    }else {
                        virtualAccounts.add(abstractVirtualAccount.preCreation(virtualAccount));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Bulk creation::{}", e.getMessage());
                    //throw new RestServiceException(e.getMessage(), e.getLocalizedMessage());
                    throw new RuntimeException();
                }
            });
            return virtualAccounts;
        }

}

