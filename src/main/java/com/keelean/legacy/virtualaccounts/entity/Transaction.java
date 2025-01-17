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
//This table should only be used to record lookups that have PAYMENTID. Its for only tracking amount request.
public class VirtualAccountTransaction extends AbstractBaseAuditableEntity {

    private Long customerId; // same as VANCustomer-> id
    private String accountID; // store VAN. Reason is for cases where VAN gets re-allocated which will mean different customerID and can be used to identify reassigned VANs
    private String transactionId;  //store PAYMENTID must NOT be null
    private LocalDateTime paymentReceivedDate;  //to be updated from payments paymentDate
    private BigDecimal actualAmount; //amount received from payment processor to be validated
    private BigDecimal expectedAmount; // amount configured against VAN
    private String validationStatus; // enum:SUCCESS/ FAILED of lookup validation accountid against amount expected
    private String txnStatus; // payments txnStatus. to be updated using subscription to kafka topic and if failure, update using rqueue by checking later by PAYMENTID

}