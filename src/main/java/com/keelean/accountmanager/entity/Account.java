package com.keelean.accountmanager.entity;



import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.TxnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
@Builder
@ToString
public abstract class Account extends AbstractBaseAuditableEntity {

    @Size(min = 5, max = 50)
    private String accountName;
    private Long amount;
    private Integer timeoutInMins;
    private Integer waitStartTime;
    private String partnerId;
    private String accountId;
    private String referenceId;
    private String invoiceRef;
    private Long minDeposit = 0L;
    private Long maxDeposit = 0L;
    private TxnStatus txnStatus = TxnStatus.UNPROCESSED;
    private AccountMode accountMode = AccountMode.DYNAMIC;


    public abstract boolean isExpired();

    public abstract boolean isAmountValid(Long amount);

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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
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
