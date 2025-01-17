package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.dto.VirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.WrapperVirtualAccountDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.example.platform.exception.RestServiceException;
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
public class VirtualAccountManager {

    private static final Integer MAX_ITEM_SIZE = 100;

    @Autowired
    private VirtualAccountFactoryProvider provider;

    @Autowired
    @Qualifier("virtualAccountTaskExecutor")
    private Executor virtualAccountExecutor;

    @SneakyThrows
    public BaseVirtualAccountResponseDto createVirtualAccount(BaseVirtualAccountRequestDto requestDto, String mode){
        //Validate Request
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(mode);
        VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(requestDto);
        virtualAccount.setReferenceId(requestDto.getReferenceId());
        return abstractVirtualAccount.preCreation(virtualAccount);
    }

    @SneakyThrows
    public BaseVirtualAccountResponseDto createVirtualAccount(VirtualAccountRequestDto requestDto){
        //Validate Request
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(requestDto.getMode().name().toLowerCase());
        VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(requestDto);
        return abstractVirtualAccount.singleFullCreation(virtualAccount);
    }


    @SneakyThrows
    public List<BaseVirtualAccountResponseDto> createVirtualAccount(WrapperVirtualAccountDto<VirtualAccountRequestDto> requestDtos){

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<VirtualAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, VirtualAccountRequestDto> treeMap = new HashMap<>();

        for (VirtualAccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseVirtualAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }




        List<BaseVirtualAccountResponseDto> virtualAccounts = new ArrayList<>();

        virtualAccounts = baseVirtualAccountRequestDtos.stream()
                .map(request -> {
                    AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(request.getMode().name().toLowerCase());
                    VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(request);
                    virtualAccount.setReferenceId(request.getReferenceId());
                    return abstractVirtualAccount.singleFullCreation(virtualAccount);
                }).collect(Collectors.toList());

        log.info("SIZE::{}", virtualAccounts.size());//5200010304
        return virtualAccounts;
    }

    @SneakyThrows
    public List<BaseVirtualAccountResponseDto> processParallelyWithExecutorService(WrapperVirtualAccountDto<BaseVirtualAccountRequestDto> requestDtos) {

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<BaseVirtualAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, BaseVirtualAccountRequestDto> treeMap = new HashMap<>();

        for (BaseVirtualAccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseVirtualAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }


        //List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();

        for (BaseVirtualAccountRequestDto dto : baseVirtualAccountRequestDtos){
            CompletableFuture<BaseVirtualAccountResponseDto> response = CompletableFuture.supplyAsync(
                    ()-> {
                        VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(dto);
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



        List<BaseVirtualAccountResponseDto> collect = futures.stream()
                .filter(b -> b != null)
                .map(b -> b.getNow(BaseVirtualAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
        return collect;
    }

    @SneakyThrows
    public List<BaseVirtualAccountResponseDto> processParallelyWithExecutorServicess(WrapperVirtualAccountDto<BaseVirtualAccountRequestDto> requestDtos) {

        if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
            throw new Exception(String.format("Items size cannot be greater than 100. You have %d items", requestDtos.getRequests().size()));
        }
        Set<BaseVirtualAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

        Map<String, BaseVirtualAccountRequestDto> treeMap = new HashMap<>();

        for (BaseVirtualAccountRequestDto dto: requestDtoSet) {
            if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                continue;
            }else{
                treeMap.put(dto.getAccountId(), dto);
            }
        }

        List<BaseVirtualAccountRequestDto> baseVirtualAccountRequestDtos;

        if(treeMap.isEmpty()){
            baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
        }else {
            baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
        }


        //List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = new ArrayList<>();
        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        List<CompletableFuture<BaseVirtualAccountResponseDto>> futures = baseVirtualAccountRequestDtos.stream().map(
                response -> CompletableFuture.supplyAsync(
                        ()-> {
                            VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(response);
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
        public List<BaseVirtualAccountResponseDto> createVirtualAccountIdsPreBulkMode(WrapperVirtualAccountDto<BaseVirtualAccountRequestDto> requestDtos) {

            if(requestDtos.getRequests().size() > MAX_ITEM_SIZE){
                log.info("Items size cannot be greater than 100. You have {} items", requestDtos.getRequests().size());
                throw new RestServiceException(ErrorCodes.ACCOUNT_LENGTH_VALIDATION.getCode(), MAX_ITEM_SIZE);
            }
            Set<BaseVirtualAccountRequestDto> requestDtoSet = new HashSet<>(requestDtos.getRequests());

            Map<String, BaseVirtualAccountRequestDto> treeMap = new HashMap<>();

            for (BaseVirtualAccountRequestDto dto: requestDtoSet) {
                if(Objects.isNull(dto.getAccountId()) || Strings.isEmpty(dto.getAccountId())){
                    continue;
                }else{
                    treeMap.put(dto.getAccountId(), dto);
                }
            }

            List<BaseVirtualAccountRequestDto> baseVirtualAccountRequestDtos;

            if(treeMap.isEmpty()){
                baseVirtualAccountRequestDtos = new ArrayList<>(requestDtoSet);
            }else {
                baseVirtualAccountRequestDtos = new ArrayList<>(treeMap.values());
            }

            List<BaseVirtualAccountResponseDto> virtualAccounts = new ArrayList<>();

            AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
            Iterable<BaseVirtualAccountRequestDto> iterable = () -> baseVirtualAccountRequestDtos.iterator();
            Stream<BaseVirtualAccountRequestDto> stream = StreamSupport.stream(iterable.spliterator(), true);
            stream.forEach(req -> {
                try {
                    req.setMode(VirtualAccountMode.DYNAMIC);
                    VirtualAccount virtualAccount = abstractVirtualAccount.createVirtualAccount(req);
                    if(virtualAccount.getTimeoutInMins() != null){
                        virtualAccounts.add(abstractVirtualAccount.singleFullCreation(virtualAccount));
                    }else {
                        virtualAccounts.add(abstractVirtualAccount.preCreation(virtualAccount));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Bulk creation::{}", e.getMessage());
                    throw new RestServiceException(e.getMessage(), e.getLocalizedMessage());
                }
            });
            return virtualAccounts;
        }

}

