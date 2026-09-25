package com.keelean.accountmanager.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class AccountRequestDto extends BaseAccountRequestDto {

    private String accountName;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Integer timeoutInMins;
    private String invoiceRef;
    private BigDecimal minDeposit;
    private BigDecimal maxDeposit;

}
