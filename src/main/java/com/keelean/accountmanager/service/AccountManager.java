package com.keelean.accountmanager.service;

import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.dto.FullAccountResponseDto;
import com.keelean.accountmanager.dto.WrapperAccountDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.EntitySessionManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class AccountManager {

    private static final Integer MAX_ITEM_SIZE = 100;

    @Autowired
    private AccountFactoryProvider provider;

    @Autowired
    private EntitySessionManager entitySessionManager;

    @SneakyThrows
    public BaseAccountResponseDto createVirtualAccount(BaseAccountRequestDto requestDto, String mode){
        //Validate Request
        if (requestDto.getAccountType() == null) {
            requestDto.setAccountType(AccountType.valueOf(mode.toUpperCase()));
        }
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

    // Bulk full creation: every account is created, or none is
    @SneakyThrows
    public List<BaseAccountResponseDto> createVirtualAccount(WrapperAccountDto<AccountRequestDto> requestDtos){
        List<AccountRequestDto> requests = deduplicate(requestDtos.getRequests());

        // Each account type has its own provider; group so each group's IDs are reserved together
        Map<AccountType, List<Integer>> positionsByType = new LinkedHashMap<>();
        for (int i = 0; i < requests.size(); i++) {
            positionsByType.computeIfAbsent(requests.get(i).getAccountType(), type -> new ArrayList<>()).add(i);
        }

        PendingCustomer[] pending = new PendingCustomer[requests.size()];
        for (Map.Entry<AccountType, List<Integer>> group : positionsByType.entrySet()) {
            AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount(group.getKey().name().toLowerCase());
            List<AccountRequestDto> groupRequests = group.getValue().stream().map(requests::get).toList();
            List<Account> accounts = abstractVirtualAccount.createVirtualAccounts(groupRequests);
            for (int j = 0; j < accounts.size(); j++) {
                pending[group.getValue().get(j)] = new PendingCustomer(abstractVirtualAccount.toFullCustomer(accounts.get(j)), true);
            }
        }

        List<BaseAccountResponseDto> virtualAccounts = saveAll(Arrays.asList(pending));
        log.info("SIZE::{}", virtualAccounts.size());
        return virtualAccounts;
    }

    // Bulk pre-creation: every account is created, or none is
    @SneakyThrows
    public List<BaseAccountResponseDto> createVirtualAccountIdsPreBulkMode(WrapperAccountDto<BaseAccountRequestDto> requestDtos) {
        List<BaseAccountRequestDto> requests = deduplicate(requestDtos.getRequests());
        requests.forEach(request -> request.setAccountType(AccountType.STATIC));

        AbstractVirtualAccount abstractVirtualAccount = provider.getVirtualAccount("dynamic");
        List<PendingCustomer> pending = new ArrayList<>(requests.size());
        for (Account virtualAccount : abstractVirtualAccount.createVirtualAccounts(requests)) {
            if (virtualAccount.getTimeoutInMins() != null) {
                pending.add(new PendingCustomer(abstractVirtualAccount.toFullCustomer(virtualAccount), true));
            } else {
                pending.add(new PendingCustomer(abstractVirtualAccount.toPreCreatedCustomer(virtualAccount), false));
            }
        }
        return saveAll(pending);
    }

    private record PendingCustomer(AccountCustomer customer, boolean fullCreation) {
    }

    private List<BaseAccountResponseDto> saveAll(List<PendingCustomer> pending) {
        entitySessionManager.saveAllCommit(pending.stream().map(PendingCustomer::customer).toList());
        return pending.stream().map(AccountManager::toResponse).toList();
    }

    private static BaseAccountResponseDto toResponse(PendingCustomer pending) {
        AccountCustomer customer = pending.customer();
        log.info("Created account {} for reference {}", customer.getAccountId(), customer.getReferenceId());
        if (pending.fullCreation()) {
            return FullAccountResponseDto.builder()
                    .accountId(customer.getAccountId())
                    .expiryDate(customer.getExpiryDate())
                    .referenceId(customer.getReferenceId())
                    .build();
        }
        return BaseAccountResponseDto.builder()
                .accountId(customer.getAccountId())
                .referenceId(customer.getReferenceId())
                .build();
    }

    // Drops repeated references; if any request supplies an account ID, only those requests are kept (one per ID)
    private <T extends BaseAccountRequestDto> List<T> deduplicate(List<T> requests) {
        if (requests.size() > MAX_ITEM_SIZE) {
            log.info("Items size cannot be greater than 100. You have {} items", requests.size());
            throw new RestServiceException(ErrorCodes.ACCOUNT_LENGTH_VALIDATION.getCode(), MAX_ITEM_SIZE);
        }
        Set<T> requestDtoSet = new HashSet<>(requests);

        Map<String, T> byAccountId = new HashMap<>();
        for (T dto : requestDtoSet) {
            if (!Objects.isNull(dto.getAccountId()) && !Strings.isEmpty(dto.getAccountId())) {
                byAccountId.put(dto.getAccountId(), dto);
            }
        }
        return byAccountId.isEmpty() ? new ArrayList<>(requestDtoSet) : new ArrayList<>(byAccountId.values());
    }
}
