package com.keelean.legacy.customeraccounts.entity;

import com.example.platform.model.base.AbstractBaseAuditableEntity;
import lombok.*;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Entity
@Builder
@ToString
public class VirtualAccountTransaction extends AbstractBaseAuditableEntity {

    private Long customerId;
    private String transactionId;
    private LocalDateTime paymentReceivedDate;
    private BigDecimal actualAmount;
}
