package com.keelean.legacy.customeraccounts.dto;

import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class VirtualAccountRequestDto extends BaseVirtualAccountRequestDto {

    private String accountName;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Integer timeoutInMins;
    private String invoiceRef;
    private BigDecimal minDeposit;
    private BigDecimal maxDeposit;

}
