package com.keelean.accountmanager.entity;



import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.TxnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
@SuperBuilder
@ToString
// Domain object built from a create request; the persisted record is AccountCustomer
public abstract class Account extends AbstractBaseAuditableEntity {

    @Size(min = 5, max = 50)
    private String accountName;
    private BigDecimal amount;
    private Integer timeoutInMins;
    private Integer waitStartTime;
    private String partnerId;
    private String accountId;
    private String referenceId;
    private String invoiceRef;
    @Builder.Default
    private Long minDeposit = 0L;
    @Builder.Default
    private Long maxDeposit = 0L;
    @Builder.Default
    private TxnStatus txnStatus = TxnStatus.UNPROCESSED;
    @Builder.Default
    private AccountMode accountMode = AccountMode.DYNAMIC;


    public abstract boolean isExpired();

    public abstract boolean isAmountValid(BigDecimal amount);

    public String displayName(String partnerName) {
        if (StringUtils.isEmpty(accountName)) {
            accountName = partnerName;
            return accountName;
        }
        return partnerName + " | " + accountName;
    }


    public boolean isTransacted() {
        return txnStatus == TxnStatus.PROCESSED;
    }


    /*
    public boolean isPaymentWindowValid() {
        return !getCreatedDate().isEqual(LocalDateTime.now());
    }*/


    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTimeoutInMins() {
        return timeoutInMins;
    }

    public Integer getWaitStartTime() {
        return waitStartTime;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getAccountId() {
        return accountId;
    }


    public String getReferenceId() {
        return referenceId;
    }

    public String getInvoiceRef() {
        return invoiceRef;
    }

    public Long getMinDeposit() {
        return minDeposit;
    }

    public Long getMaxDeposit() {
        return maxDeposit;
    }


    public TxnStatus getTxnStatus() {
        return txnStatus;
    }
}
