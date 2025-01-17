package com.keelean.legacy.customeraccounts.dto;

import com.keelean.legacy.customeraccounts.enums.AccountCapacity;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

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
    private VirtualAccountMode accountMode;
    private String templateName;
    private String routeId;
    private Integer minDeposit;
    private Integer maxDeposit;
    private boolean exactPayment;


}
