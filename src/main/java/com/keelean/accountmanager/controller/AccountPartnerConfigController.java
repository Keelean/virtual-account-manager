package com.keelean.accountmanager.controller;


import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.dto.PartnerConfigCreateRequest;
import com.keelean.accountmanager.dto.PartnerConfigResponse;
import com.keelean.accountmanager.dto.response.Response;
import com.keelean.accountmanager.service.PartnerAccountConfigService;
import com.keelean.accountmanager.utils.AppUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(AppConstants.VIRTUAL_ACCOUNT_PARTNER_CONFIG)
@AllArgsConstructor
public class AccountPartnerConfigController {

    private final PartnerAccountConfigService partnerConfigService;

    @PostMapping
    Response<PartnerConfigResponse> create(@RequestBody @Valid PartnerConfigCreateRequest configCreateRequest){
        PartnerConfigResponse partnerConfigResponse = partnerConfigService.create(configCreateRequest);
        Response<PartnerConfigResponse> rv = new Response<>();
        rv.setData(partnerConfigResponse);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @PostMapping("/{configId}/{partnerId}")
    Response<PartnerConfigResponse> update(@PathVariable Long configId, @PathVariable String partnerId, @RequestBody @Valid PartnerConfigCreateRequest configCreateRequest){
        PartnerConfigResponse responseDto = partnerConfigService.update(configId, partnerId, configCreateRequest);
        Response<PartnerConfigResponse> rv = new Response<>();
        rv.setData(responseDto);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping("/{partnerId}")
    Response<List<PartnerConfigResponse>> getConfig(@PathVariable String partnerId){
        List<PartnerConfigResponse> partnerConfigResponses = partnerConfigService.findByPartnerId(partnerId);
        Response<List<PartnerConfigResponse>> rv = new Response<>();
        rv.setData(partnerConfigResponses);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }
}
