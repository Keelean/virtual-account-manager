package com.keelean.legacy.customeraccounts.entity;


import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class VirtualAccountMeta {

    private String accountName;
    private BigDecimal amount;
    private VirtualAccountMode mode;
    private LocalDateTime waitStartTime;
    private Integer minMultiplier;
    private Integer maxMultiplier;
}
