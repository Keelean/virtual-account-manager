package com.keelean.accountmanager.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@SuperBuilder
@ToString
public class AccountCustomerResponseDto extends BaseAccountResponseDto {

    private String accountName;
    private LocalDateTime expiryDate;
    private BigDecimal amount;
    private String nickName;
    private String status;
}
