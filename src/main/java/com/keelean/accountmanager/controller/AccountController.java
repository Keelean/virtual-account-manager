package com.keelean.accountmanager.controller;


import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.dto.AccountCustomerResponseDto;
import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.dto.AccountUpdateRequestDto;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.dto.PageableResponse;
import com.keelean.accountmanager.dto.WrapperAccountDto;
import com.keelean.accountmanager.dto.response.Response;
import com.keelean.accountmanager.service.AccountCustomerService;
import com.keelean.accountmanager.service.AccountManager;
import com.keelean.accountmanager.utils.AppUtils;
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
public class AccountController {

    private AccountManager virtualAccountManager;

    private AccountCustomerService virtualAccountCustomerService;

    @PostMapping
    Response<BaseAccountResponseDto> generateVirtualAccount(@RequestBody @Valid AccountRequestDto requestDto){
        log.info("VirtualAccountRequestDto::[{}]",requestDto);
        BaseAccountResponseDto virtualAccount = virtualAccountManager.createVirtualAccount(requestDto);
        Response<BaseAccountResponseDto> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }


    @PostMapping("/pre")
    Response<BaseAccountResponseDto> generateVirtualAccountPreDynamic(@RequestBody BaseAccountRequestDto requestDto){
        BaseAccountResponseDto virtualAccount = virtualAccountManager.createVirtualAccount(requestDto, "dynamic");
        Response<BaseAccountResponseDto> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }


    @PostMapping("/preBulk")
    Response<List<BaseAccountResponseDto>> generateVirtualAccountPreBulk(@RequestBody WrapperAccountDto<BaseAccountRequestDto> requestDto){
        List<BaseAccountResponseDto> virtualAccount = virtualAccountManager.createVirtualAccountIdsPreBulkMode(requestDto);
        Response<List<BaseAccountResponseDto>> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @PostMapping("/bulk")
    Response<List<BaseAccountResponseDto>> generateVirtualAccountBulk(@RequestBody WrapperAccountDto<AccountRequestDto> requestDto){
        List<BaseAccountResponseDto> virtualAccount = virtualAccountManager.createVirtualAccount(requestDto);
        Response<List<BaseAccountResponseDto>> rv = new Response<>();
        rv.setData(virtualAccount);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @PostMapping("/{accountId}/{partnerId}")
    Response<BaseAccountResponseDto> updateVirtualAccount(@PathVariable String accountId, @PathVariable String partnerId, @RequestBody @Valid AccountUpdateRequestDto requestDto){
        BaseAccountResponseDto responseDto = virtualAccountCustomerService.update(requestDto, accountId, partnerId);
        Response<BaseAccountResponseDto> rv = new Response<>();
        rv.setData(responseDto);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping("/{accountIdOrReferenceId}")
    Response<AccountCustomerResponseDto> getCustomerVirtualAccount(@PathVariable String accountIdOrReferenceId){
        AccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.getVirtualAccountFromList(accountIdOrReferenceId);
        Response<AccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping
    Response<AccountCustomerResponseDto> getCustomerVirtualAccountWithAmount(@RequestParam(name = "accountId") String accountIdOrReferenceId, @RequestParam(name = "amount") BigDecimal txnAmount){
        AccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.findVirtualAccountAndValidateAmount(accountIdOrReferenceId, txnAmount);
        Response<AccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID");
        return rv;
    }

    @GetMapping("/all")
    PageableResponse<AccountCustomerResponseDto> getVirtualAccountByPartnerId(@RequestParam(name = "partnerId") String partnerId, Pageable pageable){
        PageableResponse<AccountCustomerResponseDto> pageableResponse = new PageableResponse<>();
        AppUtils
                .setSuccessResponse(pageableResponse, virtualAccountCustomerService.getAll(partnerId, pageable));
        return pageableResponse;
    }
    @PostMapping("/refresh/{accountIdOrReferenceId}/{partnerId}")
    Response<AccountCustomerResponseDto> refreshVirtualAccount(@PathVariable String accountIdOrReferenceId, @PathVariable String partnerId){
        AccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.refreshAccountId(accountIdOrReferenceId, partnerId);
        Response<AccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID{}","00");
        return rv;
    }

    @PostMapping("/close/{accountIdOrReferenceId}/{partnerId}")
    Response<AccountCustomerResponseDto> closeVirtualAccount(@PathVariable String accountIdOrReferenceId, @PathVariable String partnerId){
        AccountCustomerResponseDto virtualAccountCustomer = virtualAccountCustomerService.closeVirtualAccount(accountIdOrReferenceId, partnerId);
        Response<AccountCustomerResponseDto> rv = new Response<>();
        rv.setData(virtualAccountCustomer);
        AppUtils.setSuccessResponse(rv);
        log.info("Exiting request to get account ID::{}", "00");
        return rv;
    }

}
