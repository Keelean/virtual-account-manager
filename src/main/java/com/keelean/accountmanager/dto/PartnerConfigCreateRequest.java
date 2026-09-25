package com.keelean.accountmanager.dto;


import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PartnerConfigCreateRequest implements Serializable {

    @NotNull
    @NotBlank
    private String partnerId;
    private String code;
    @NotNull
    @NotBlank
    private String accountPrefix;
    @NotNull
    private AccountCapacity capacity;

    @NotNull
    private String defaultLookUpDisplayName;
    private String gradeCode;
    private String accountDetails;
    @NotNull
    private AccountType accountType;
    private String templateName;
    private String routeId;
    private Integer minDeposit;
    private Integer maxDeposit;
    private boolean exactPayment;


}
