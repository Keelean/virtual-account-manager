package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.dto.PartnerConfigCreateRequest;
import com.keelean.legacy.customeraccounts.dto.PartnerConfigResponse;
import com.keelean.legacy.customeraccounts.entity.PartnerConfig;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountPartnerConfig;
import com.keelean.legacy.customeraccounts.enums.ConfigStatus;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountPartnerConfigRepo;
import com.example.platform.dao.EntitySessionManager;
import com.example.platform.exception.RestServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class VirtualAccountPartnerConfigService {

    private VirtualAccountPartnerConfigRepo repository;
    private EntitySessionManager entitySessionManager;

    public List<VirtualAccountPartnerConfig> findPartnerConfigByIdentifierAndStatus(String partnerId){
        return repository.findByPartnerIdAndStatus(partnerId, ConfigStatus.ACTIVE);
    }

    public List<VirtualAccountPartnerConfig> findPartnerConfigByIdentifier(String partnerId){
        return findPartnerConfigByIdentifierAndStatus(partnerId);
    }

    public PartnerConfigResponse create(PartnerConfigCreateRequest request){

        List<VirtualAccountPartnerConfig> configs;

        if(StringUtils.isBlank(request.getPartnerId())){
            throw new RestServiceException(ErrorCodes.INVALID_PARTNER_CODE.getCode(), request.getPartnerId());
        }

        configs = repository.findByPartnerId(request.getPartnerId());

        VirtualAccountPartnerConfig vaConfig;

        if(configs.isEmpty()){
            vaConfig = createConfig(request);
        } else if (configs.size() >= 2) {
            log.info("Configuration already exist1!");
            throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_ALREADY_EXIST.getCode(), request.getAccountMode().name());
        } else {
            configs = configs.stream().filter(c -> c.getMeta().getMode() == request.getAccountMode()).collect(Collectors.toList());
            if(configs.isEmpty()){
                vaConfig = createConfig(request);
            }else {
                log.info("Configuration already exist2!");
                throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_ALREADY_EXIST.getCode(), request.getAccountMode().name());
            }

        }

        return PartnerConfigResponse.builder()
                .partnerCode(vaConfig.getCode())
                .capacity(vaConfig.getCapacity().name())
                .partnerId(vaConfig.getPartnerId())
                .prefix(vaConfig.getPrefix())
                .configId(vaConfig.getId())
                .build();
    }

    private VirtualAccountPartnerConfig createConfig(PartnerConfigCreateRequest request) {
        VirtualAccountPartnerConfig vaConfig = composePartnerConfig(request);
        return entitySessionManager.saveOrUpdateCommit(vaConfig);
    }

    private VirtualAccountPartnerConfig composePartnerConfig(PartnerConfigCreateRequest request) {
        boolean exactPayment = request.isExactPayment();
        Integer minDeposit = request.getMinDeposit();
        Integer maxDeposit = request.getMaxDeposit();

        if (request.getAccountMode() == VirtualAccountMode.DYNAMIC){
            exactPayment = true;
            minDeposit = 0;
            maxDeposit = 0;
        }

        if(request.getAccountMode() == VirtualAccountMode.STATIC && !request.isExactPayment()){
            boolean isMinDepositValid = minDeposit >= 1 && minDeposit <= 10;
            boolean isMaxDepositValid = maxDeposit >= 10 && maxDeposit <= 100;

            if(!isMinDepositValid || !isMaxDepositValid){
                throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            }

        }

        return VirtualAccountPartnerConfig.builder()
                .meta(PartnerConfig.builder()
                        .accountDetails(request.getAccountDetails())
                        .mode(request.getAccountMode())
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

    public PartnerConfigResponse update(Long config, String partnerId){

        Optional<VirtualAccountPartnerConfig> partnerConfigOptional = repository.findById(config);
        if(!partnerConfigOptional.isPresent()){
            throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
        }

        VirtualAccountPartnerConfig virtualAccountPartnerConfig = partnerConfigOptional.get();
        if(!virtualAccountPartnerConfig.getPartnerId().equals(partnerId)){
            throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
        }

        return PartnerConfigResponse.builder()
                .configId(Long.valueOf("2"))
                .build();
    }

    public List<PartnerConfigResponse> findByPartnerId(Long configId, String partnerId){
        List<VirtualAccountPartnerConfig> partnerConfigs = repository.findByPartnerId(partnerId);

        if(partnerConfigs.isEmpty()){
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().filter(c -> c.getPartnerId().equals(partnerId)).map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public List<PartnerConfigResponse> findByPartnerId(Long configId){
        Optional<VirtualAccountPartnerConfig> partnerConfigs = repository.findById(configId);

        if(partnerConfigs.isEmpty()){
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public List<PartnerConfigResponse> findByPartnerId(String partnerId){
        List<VirtualAccountPartnerConfig> partnerConfigs = repository.findByPartnerId(partnerId);

        if(partnerConfigs.isEmpty()){
            return List.of(PartnerConfigResponse.builder().build());
        }

        return partnerConfigs.stream().map(c -> convertToPartnerResponse(c)).collect(Collectors.toList());
    }

    public PartnerConfigResponse findByPartnerId(Long configId, VirtualAccountMode mode){
        List<PartnerConfigResponse> partnerConfigResponses = findByPartnerId(configId);

        return partnerConfigResponses.stream().filter(c -> c.getMode().equals(mode.name())).collect(Collectors.toList()).stream().findFirst().orElseGet(() -> null);
    }

    public PartnerConfigResponse findByPartnerId(String partnerId, VirtualAccountMode mode){
        List<PartnerConfigResponse> partnerConfigResponses = findByPartnerId(partnerId);

        return partnerConfigResponses.stream().filter(c -> c.getMode().equals(mode.name())).collect(Collectors.toList()).stream().findFirst().orElseGet(() -> null);
    }

    PartnerConfigResponse convertToPartnerResponse(VirtualAccountPartnerConfig partnerConfig){
        return PartnerConfigResponse.builder()
                .prefix(partnerConfig.getPrefix())
                .partnerCode(partnerConfig.getCode())
                .capacity(partnerConfig.getCapacity().name())
                .partnerId(partnerConfig.getPartnerId())
                .mode(partnerConfig.getMeta().getMode().name())
                .minDeposit(partnerConfig.getMeta().getMinMultiplier())
                .maxDeposit(partnerConfig.getMeta().getMaxMultiplier())
                .exactPayment(partnerConfig.getMeta().isExactPayment())
                .partnerName(partnerConfig.getMeta().getDefaultLookupDisplayName())
                .configId(partnerConfig.getId())
                .build();
    }
}


