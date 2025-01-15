package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.config.ApplicationProperties;
import com.keelean.legacy.customeraccounts.constants.AppConstants;
import com.keelean.legacy.customeraccounts.enums.KYCSource;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.example.core.notification.enums.NotificationType;
import com.example.platform.exception.RestServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class AccountValidationService {

    @Autowired
    NotificationHelperService notificationHelperService;

    @Autowired
    ApplicationProperties.NotificationProperties notificationProperties;

    @Autowired
    KYCHelperService kycHelperService;

    public LinkageValidationResponse validateLinkage(LinkageValidationRequestDto validationRequest){
        log.info("Entering validateLinkage flow for msisdn : {}", validationRequest.getMsisdn());
        log.debug("Entering validateLinkage flow with request : {}", validationRequest.toString());
        String msisdn = validationRequest.getMsisdn();
        notificationHelperService.validateOtp(msisdn, notificationProperties.getKycValidationProcessName(), NotificationType.SMS,
                validationRequest.getOtp(), new HashMap<>());
        Map<String, Object> kycResponse = kycHelperService.getKycDetails(KYCSource.AM, msisdn);
        Map<String, Object> kycResponseData = (Map<String, Object>) kycResponse.get(AppConstants.RESPONSE);
        LinkageValidationResponse validationResponse = new LinkageValidationResponse();
        if(validationRequest.getValidateKYC()){
            validatePartnerKYCDetails(validationRequest, kycResponseData, validationResponse);
        }else{
            validationResponse.setValidationSuccessful(Boolean.TRUE);
            validationResponse.setKycData(createKYCData(kycResponseData));

        }
        log.info("Exiting validateLinkage flow for msisdn : {}", validationRequest.getMsisdn());
        log.debug("Exiting validateLinkage flow with request : {}", validationResponse.toString());
        return validationResponse;
    }

    private void validatePartnerKYCDetails(LinkageValidationRequestDto validationRequest, Map<String, Object> kycResponseData,
                                                                LinkageValidationResponse response){
        log.info("Entering validatePartnerKYCDetails for msisdn : {}", validationRequest.getMsisdn());
        if(StringUtils.isBlank(validationRequest.getFirstName()) ||
                StringUtils.isBlank(validationRequest.getLastName()) ||
                StringUtils.isBlank(validationRequest.getIdNumber()) ||
                ObjectUtils.isEmpty(validationRequest.getDateOfBirth())){
            throw new RestServiceException(ErrorCodes.MISSING_DATA_FOR_LINKAGE_VALIDATION.getCode());
        }else if(ObjectUtils.isEmpty(kycResponseData.get(AppConstants.FIRST_NAME)) ||
                ObjectUtils.isEmpty(kycResponseData.get(AppConstants.LAST_NAME)) ||
                ObjectUtils.isEmpty(kycResponseData.get(AppConstants.ID_NUMBER)) ||
                ObjectUtils.isEmpty(kycResponseData.get(AppConstants.DATE_OF_BIRTH))){
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.ISO_LOCAL_DATE_FORMAT, Locale.US);
        if(StringUtils.equals((CharSequence) kycResponseData.get(AppConstants.FIRST_NAME), validationRequest.getFirstName()) &&
        StringUtils.equals((CharSequence) kycResponseData.get(AppConstants.LAST_NAME), validationRequest.getLastName()) &&
        StringUtils.equals((CharSequence) kycResponseData.get(AppConstants.ID_NUMBER), validationRequest.getIdNumber()) &&
        validationRequest.getDateOfBirth().equals(
                LocalDate.parse(kycResponseData.get(AppConstants.DATE_OF_BIRTH).toString().substring(0,10), formatter))){
            response.setValidationSuccessful(Boolean.TRUE);
        }
    }

    private Map<String, Object> createKYCData(Map<String, Object> kycResponseData){
        log.info("Entering createKYCData for msisdn : {}", kycResponseData.get(AppConstants.MSISDN_KEY));
        Map<String, Object> kycData = new HashMap<>();
        kycData.put(AppConstants.FIRST_NAME, kycResponseData.get(AppConstants.FIRST_NAME));
        kycData.put(AppConstants.LAST_NAME, kycResponseData.get(AppConstants.LAST_NAME));
        kycData.put(AppConstants.DATE_OF_BIRTH, kycResponseData.get(AppConstants.DATE_OF_BIRTH));
        kycData.put(AppConstants.ID_NUMBER, kycResponseData.get(AppConstants.ID_NUMBER));
        return kycData;
    }
}
