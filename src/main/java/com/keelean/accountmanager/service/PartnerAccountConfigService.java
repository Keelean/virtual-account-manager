package com.keelean.accountmanager.service;

import com.keelean.accountmanager.dto.PartnerConfigCreateRequest;
import com.keelean.accountmanager.dto.PartnerConfigResponse;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.entity.PartnerAccountConfigMeta;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.enums.ConfigStatus;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.repo.EntitySessionManager;
import com.keelean.accountmanager.repo.PartnerAccountConfigRepo;
import com.keelean.accountmanager.utils.AppUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PartnerAccountConfigService {

    private PartnerAccountConfigRepo repository;
    private EntitySessionManager entitySessionManager;
    private AccountPoolService accountPoolService;
    @Value("${va-pool.dynamic.shared-pool-max-partners}")
    private Integer maxPartnersInSharedDynamicPool;
    @Value("${va-pool.static.shared-pool-max-partners}")
    private Integer maxPartnersInSharedStaticPool;

    public List<PartnerAccountConfig> findPartnerConfigByIdentifierAndStatus(String partnerId) {
        return repository.findByPartnerIdAndStatus(partnerId, ConfigStatus.ACTIVE);
    }

    public List<PartnerAccountConfig> findPartnerConfigByIdentifier(String partnerId) {
        return findPartnerConfigByIdentifierAndStatus(partnerId);
    }

    private PartnerAccountConfig createConfig(PartnerConfigCreateRequest request) {
        PartnerAccountConfig partnerAccountConfig = mapRequestToEntity(request);
        if (partnerAccountConfig.isSharedPool())
            //check the number of prefix allocation is more than 1000
            allocateSharedPool(partnerAccountConfig, request);
        else
            allocateDedicatedPool(partnerAccountConfig, request.getAccountPrefix());

        partnerAccountConfig.setStatus(ConfigStatus.ACTIVE);
        return entitySessionManager.saveOrUpdateCommit(partnerAccountConfig);
    }

    private void allocateDedicatedPool(PartnerAccountConfig partnerAccountConfig, String prefixSeries) {
        //find partners with prefix series
        Optional<String> partnersAccountPrefix;
        AccountPool accountPool = accountPoolService.findPoolByPrefixSeries(Integer.parseInt(prefixSeries));
        Set<String> sharedExclusivePrefix = new HashSet<>(accountPool.getExcludedPrefixStart());
        List<PartnerAccountConfig> partnerAccountConfigs = repository.findByPrefixStartsWith(partnerAccountConfig.getPrefix());

        Set<String> allPartnersStartPrefix = partnerAccountConfigs.stream().map(PartnerAccountConfig::getPrefix).collect(Collectors.toSet());
        allPartnersStartPrefix.addAll(sharedExclusivePrefix);

        if (allPartnersStartPrefix.isEmpty()) {
            partnersAccountPrefix = Optional.of(AppUtils.formatStartSequence(AccountCapacity.getStartPrefixWidth(partnerAccountConfig.getCapacity()),
                    Integer.parseInt(partnerAccountConfig.getPrefix()), 0));
        } else {
            partnersAccountPrefix = assignPrefixNotAlreadyAllocated(partnerAccountConfig.getCapacity(), prefixSeries, allPartnersStartPrefix);
        }
        partnerAccountConfig.setPrefix(partnersAccountPrefix.get());
        partnerAccountConfig.setCurrentSequence(-1);
        entitySessionManager.saveOrUpdateCommit(partnerAccountConfig);
    }

    private Optional<String> assignPrefixNotAlreadyAllocated(AccountCapacity capacity, String prefixSeries, Set<String> allPrefixes) {
        int prefixWidth = AccountCapacity.getStartPrefixWidth(capacity);
        Optional<String> partnersAccountPrefix = Optional.empty();
        //Loop through all possible allocations
        for (int allocation = 0; allocation < (int) Math.pow(10.0, prefixWidth); allocation++) {
            partnersAccountPrefix = Optional.of(AppUtils.formatStartSequence(prefixWidth, Integer.parseInt(prefixSeries), allocation));
            if (!allPrefixes.contains(partnersAccountPrefix)) {
                break;
            }
        }

        if (partnersAccountPrefix.isEmpty()) {
            throw new IllegalArgumentException("Dedicated pool for series is exhausted!");
        }
        return partnersAccountPrefix;
    }

    private void allocateSharedPool(PartnerAccountConfig partnerAccountConfig, PartnerConfigCreateRequest request) {
        AccountPool accountPool = accountPoolService.findPoolByPrefixSeries(Integer.parseInt(request.getAccountPrefix()));
        validateSharedPoolAllocation(accountPool);
        if (request.getAccountType() == AccountType.DYNAMIC && accountPool.getAllocationCount() + 1 <= maxPartnersInSharedDynamicPool) {
            accountPool.setAllocationCount(accountPool.getAllocationCount() + 1);
        } else if (request.getAccountType() == AccountType.STATIC && accountPool.getAllocationCount() + 1 <= maxPartnersInSharedStaticPool) {
            accountPool.setAllocationCount(accountPool.getAllocationCount() + 1);
        }

        partnerAccountConfig.setPrefix(String.valueOf(accountPool.getStartPrefix()));
        accountPool.setAllocationCount(accountPool.getAllocationCount() + 1);
        accountPoolService.saveOrUpdatePool(accountPool);
    }

    private void validateSharedPoolAllocation(AccountPool accountPool) {
        if (accountPool.isClosed()) {
            throw new IllegalArgumentException("Shared pool is exhausted! Please create a new pool and try again!");
        }

        if (accountPool.isSharedPoolAllocationFull()) {
            throw new IllegalArgumentException("Shared pool allocation is exhausted! Please create a new pool and try again!");
        }
    }

    private PartnerAccountConfig mapRequestToEntity(PartnerConfigCreateRequest request) {
        boolean exactPayment = request.isExactPayment();
        Integer minDeposit = request.getMinDeposit();
        Integer maxDeposit = request.getMaxDeposit();

        if (request.getAccountType() == AccountType.DYNAMIC && !request.isExactPayment() && minDeposit == null && maxDeposit == null) {
            exactPayment = true;
            minDeposit = 0;
            maxDeposit = 0;
        }

        if (request.getAccountType() == AccountType.STATIC) {
            boolean isMinDepositValid = minDeposit >= 1 && minDeposit <= 10;
            boolean isMaxDepositValid = maxDeposit >= 10 && maxDeposit <= 100;

            if (!isMinDepositValid || !isMaxDepositValid) {
                throw new RuntimeException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            }

        }

        return PartnerAccountConfig.builder()
                .meta(PartnerAccountConfigMeta.builder()
                        .accountDetails(request.getAccountDetails())
                        .accountType(request.getAccountType())
                        .gradeCode(request.getGradeCode())
                        .templateName(request.getTemplateName())
                        .routeId(request.getRouteId())
                        .defaultLookupDisplayName(request.getDefaultLookUpDisplayName())
                        .exactPayment(exactPayment)
                        .minMultiplier(minDeposit)
                        .maxMultiplier(maxDeposit)
                        .build())
                .code(request.getCode())
                .partnerId(request.getPartnerId())
                .status(ConfigStatus.DISABLED)
                .capacity(request.getCapacity())
                .prefix(request.getAccountPrefix())
                .build();
    }

    public PartnerConfigResponse update(Long config, String partnerId) {

        Optional<PartnerAccountConfig> partnerConfigOptional = repository.findById(config);
        if (!partnerConfigOptional.isPresent()) {
            //throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            throw new RuntimeException();
        }

        PartnerAccountConfig virtualAccountPartnerConfig = partnerConfigOptional.get();
        if (!virtualAccountPartnerConfig.getPartnerId().equals(partnerId)) {
            //throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            throw new RuntimeException();
        }

        return PartnerConfigResponse.builder()
                .configId(Long.valueOf("2"))
                .build();
    }

    public void update(PartnerAccountConfig partnerAccountConfig) {
        repository.save(partnerAccountConfig);
    }

    public List<PartnerConfigResponse> findByPartnerId(Long configId, String partnerId) {
        List<PartnerAccountConfig> partnerConfigs = repository.findByPartnerId(partnerId);

        if (partnerConfigs.isEmpty()) {
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().filter(c -> c.getPartnerId().equals(partnerId)).map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public List<PartnerConfigResponse> findByPartnerId(Long configId) {
        Optional<PartnerAccountConfig> partnerConfigs = repository.findById(configId);

        if (partnerConfigs.isEmpty()) {
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public List<PartnerConfigResponse> findByPartnerId(String partnerId) {
        List<PartnerAccountConfig> partnerConfigs = repository.findByPartnerId(partnerId);

        if (partnerConfigs.isEmpty()) {
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public PartnerConfigResponse findByPartnerId(Long configId, AccountType accountType) {
        List<PartnerConfigResponse> partnerConfigResponses = findByPartnerId(configId);

        return partnerConfigResponses.stream().filter(c -> c.getMode().equals(accountType.name())).collect(Collectors.toList()).stream().findFirst().orElseGet(() -> null);
    }

    public PartnerConfigResponse findByPartnerId(String partnerId, AccountType accountType) {
        List<PartnerConfigResponse> partnerConfigResponses = findByPartnerId(partnerId);

        return partnerConfigResponses.stream().filter(c -> c.getMode().equals(accountType.name())).collect(Collectors.toList()).stream().findFirst().orElseGet(() -> null);
    }

    PartnerConfigResponse convertToPartnerResponse(PartnerAccountConfig partnerConfig) {
        return PartnerConfigResponse.builder()
                .prefix(partnerConfig.getPrefix())
                .partnerCode(partnerConfig.getCode())
                .capacity(partnerConfig.getCapacity().name())
                .partnerId(partnerConfig.getPartnerId())
                .mode(partnerConfig.getMeta().getAccountType().name())
                .minDeposit(partnerConfig.getMeta().getMinMultiplier())
                .maxDeposit(partnerConfig.getMeta().getMaxMultiplier())
                .exactPayment(partnerConfig.getMeta().isExactPayment())
                .partnerName(partnerConfig.getMeta().getDefaultLookupDisplayName())
                .configId(partnerConfig.getId())
                .build();
    }

    public PartnerConfigResponse create(PartnerConfigCreateRequest request) {

        List<PartnerAccountConfig> configs;

        if (StringUtils.isBlank(request.getPartnerId())) {
            throw new RuntimeException(ErrorCodes.INVALID_PARTNER_CODE.getCode(), null);
        }
        configs = repository.findByPartnerId(request.getPartnerId());

        PartnerAccountConfig partnerAccountConfig;

        if (configs.isEmpty()) {
            partnerAccountConfig = createConfig(request);
        } else if (configs.size() >= 2) {
            log.info("Configuration already exist!");
            //throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_ALREADY_EXIST.getCode(), request.getAccountType().name());
            throw new RuntimeException();
        } else {
            configs = configs.stream().filter(c -> c.getMeta().getAccountType() == request.getAccountType()).collect(Collectors.toList());
            if (configs.isEmpty()) {
                partnerAccountConfig = createConfig(request);
            } else {
                log.info("Configuration already exist2!");
                //throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_ALREADY_EXIST.getCode(), request.getAccountType().name());
                throw new RuntimeException();
            }

        }

        return PartnerConfigResponse.builder()
                .partnerCode(partnerAccountConfig.getCode())
                .capacity(partnerAccountConfig.getCapacity().name())
                .partnerId(partnerAccountConfig.getPartnerId())
                .prefix(partnerAccountConfig.getPrefix())
                .configId(partnerAccountConfig.getId())
                .build();
    }


    public void createNewConfig(PartnerConfigCreateRequest partnerConfigCreateRequest) {
        List<PartnerAccountConfig> partnerAccountConfigs;

        if (StringUtils.isBlank(partnerConfigCreateRequest.getPartnerId())) {
            //throw new RestServiceException(ErrorCodes.INVALID_PARTNER_CODE.getCode(), partnerConfigCreateRequest.getPartnerId());
            throw new RuntimeException();
        }

        partnerAccountConfigs = repository.findByPartnerId(partnerConfigCreateRequest.getPartnerId());

        PartnerAccountConfig partnerAccountConfig;

        if (partnerAccountConfigs.isEmpty()) {
            partnerAccountConfig = createConfig(partnerConfigCreateRequest);
        } else {
            partnerAccountConfigs = partnerAccountConfigs.stream().filter(c -> c.getMeta().getAccountType() == partnerConfigCreateRequest.getAccountType()).collect(Collectors.toList());
            if (partnerAccountConfigs.isEmpty()) {
                partnerAccountConfig = createConfig(partnerConfigCreateRequest);
            } else {
                log.info("Configuration already exist 2!");
                //throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_ALREADY_EXIST.getCode(), partnerConfigCreateRequest.getAccountType().name());
                throw new RuntimeException();
            }
        }
    }


    //@Cacheable(value = AppConstants.BANK_CODE_CACHE, key = "{#partnerId}")
    public PartnerAccountConfig getPartnerAccountConfig(AccountType accountType, String partnerId) {
        List<PartnerAccountConfig> configs = findPartnerConfigByIdentifier(partnerId);

        if (configs.isEmpty()) {
            log.info("Partner config does not exist! Contact admin!");
            //throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
            throw new RuntimeException();
        }

        Optional<PartnerAccountConfig> partnerConfigOptional = configs.stream().filter(c -> c.getMeta().getAccountType() == accountType).findFirst();
        log.info("VirtualAccountPartnerConfig22::{}", partnerConfigOptional);
        if (partnerConfigOptional.isEmpty()) {
            log.info("Partner config does not exist! Contact admin!");
            throw new RuntimeException();
            //throw new RuntimeException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), String.format("Partner ID=%s, Account mode=%s", partnerId, accountType));
        }
        return partnerConfigOptional.get();
    }
}


