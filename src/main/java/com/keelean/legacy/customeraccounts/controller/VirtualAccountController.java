package com.keelean.legacy.customeraccounts.controller;

import com.keelean.legacy.customeraccounts.constants.AppConstants;
import com.keelean.legacy.customeraccounts.dto.*;
import com.keelean.legacy.customeraccounts.service.VirtualAccountCustomerService;
import com.keelean.legacy.customeraccounts.service.VirtualAccountManager;
import com.example.platform.dto.PageableResponse;
import com.example.platform.dto.Response;
import com.example.platform.utilities.AppUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(AppConstants.VIRTUAL_ACCOUNT_BASE_URL)
@AllArgsConstructor
public class VirtualAccountController {

    private VirtualAccountManager virtualAccountManager;

    private VirtualAccountCustomerService virtualAccountCustomerService;

    @PostMapping
    Response<BaseVirtualAccountResponseDto> generateVirtualAccount(@RequestBody @Valid VirtualAccountRequestDto requestDto){
        log.info("VirtualAccountRequestDto::[{}]",requestDto);
        BaseVirtualAccountResponseDto virtualAccount = virtualAccountManager.createVirtualAccount(requestDto);
        Response<BaseVirtualAccountResponseDto> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }


    @PostMapping("/pre")
    Response<BaseVirtualAccountResponseDto> generateVirtualAccountPreDynamic(@RequestBody BaseVirtualAccountRequestDto requestDto){
        BaseVirtualAccountResponseDto virtualAccount = virtualAccountManager.createVirtualAccount(requestDto, "dynamic");
        Response<BaseVirtualAccountResponseDto> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }


    @PostMapping("/preBulk")
    Response<List<BaseVirtualAccountResponseDto>> generateVirtualAccountPreBulk(@RequestBody WrapperVirtualAccountDto<BaseVirtualAccountRequestDto> requestDto){
        List<BaseVirtualAccountResponseDto> virtualAccount = virtualAccountManager.createVirtualAccountIdsPreBulkMode(requestDto);
        Response<List<BaseVirtualAccountResponseDto>> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @PostMapping("/bulk")
    Response<List<BaseVirtualAccountResponseDto>> generateVirtualAccountBulk(@RequestBody WrapperVirtualAccountDto<VirtualAccountRequestDto> requestDto){
        List<BaseVirtualAccountResponseDto> virtualAccount = virtualAccountManager.createVirtualAccount(requestDto);
        Response<List<BaseVirtualAccountResponseDto>> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @PostMapping("/{accountId}/{partnerId}")
    Response<BaseVirtualAccountResponseDto> updateVirtualAccount(@PathVariable String accountId, @PathVariable String partnerId, @RequestBody @Valid VirtualAccountUpdateRequestDto requestDto){
        BaseVirtualAccountResponseDto responseDto = virtualAccountCustomerService.update(requestDto, accountId, partnerId);
        Response<BaseVirtualAccountResponseDto> rv = new Response<>();
        rv.setData(responseDto);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping("/{accountIdOrReferenceId}")
    Response<VirtualAccountCustomerResponseDto> getCustomerVirtualAccount(@PathVariable String accountIdOrReferenceId){
        VirtualAccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.getVirtualAccountFromList(accountIdOrReferenceId);
        Response<VirtualAccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping
    Response<VirtualAccountCustomerResponseDto> getCustomerVirtualAccountWithAmount(@RequestParam(name = "accountId") String accountIdOrReferenceId, @RequestParam(name = "amount") BigDecimal txnAmount){
        VirtualAccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.findVirtualAccountAndValidateAmount(accountIdOrReferenceId, txnAmount);
        Response<VirtualAccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping("/all")
    PageableResponse<VirtualAccountCustomerResponseDto> getVirtualAccountByPartnerId(@RequestParam(name = "partnerId") String partnerId, Pageable pageable){
        PageableResponse<VirtualAccountCustomerResponseDto> pageableResponse = new PageableResponse<>();
        AppUtils
                .setSuccessResponse(pageableResponse, virtualAccountCustomerService.getAll(partnerId, pageable));
        return pageableResponse;
    }
    @PostMapping("/refresh/{accountIdOrReferenceId}/{partnerId}")
    Response<VirtualAccountCustomerResponseDto> refreshVirtualAccount(@PathVariable String accountIdOrReferenceId, @PathVariable String partnerId){
        VirtualAccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.refreshAccountId(accountIdOrReferenceId, partnerId);
        Response<VirtualAccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID{}","00");
        return rv;
    }

    @PostMapping("/close/{accountIdOrReferenceId}/{partnerId}")
    Response<VirtualAccountCustomerResponseDto> closeVirtualAccount(@PathVariable String accountIdOrReferenceId, @PathVariable String partnerId){
        VirtualAccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.closeVirtualAccount(accountIdOrReferenceId, partnerId);
        Response<VirtualAccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID::{}", "00");
        return rv;
    }

}
