package com.keelean.legacy.customeraccounts.dto;

import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VirtualAccountUpdateRequestDto implements Serializable {

    private String accountName;
    private BigDecimal amount;
    private Integer timeoutInMins;
    private String invoiceRef;
}
