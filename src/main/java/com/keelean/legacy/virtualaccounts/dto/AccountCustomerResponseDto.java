package com.keelean.legacy.customeraccounts.dto;

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
public class VirtualAccountCustomerResponseDto extends BaseVirtualAccountResponseDto {

    private String accountName;
    private LocalDateTime expiryDate;
    private BigDecimal amount;
    private String nickName;
    private String status;
}
