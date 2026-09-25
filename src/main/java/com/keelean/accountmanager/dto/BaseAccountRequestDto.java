package com.keelean.accountmanager.dto;

import com.keelean.accountmanager.enums.AccountType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@ToString
public class BaseAccountRequestDto implements Serializable {
    @NotNull
    private String partnerId;
    @NotNull
    private String referenceId;
    private String accountId;
    private String invoiceRef;
    private AccountType accountType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseAccountRequestDto)) return false;
        BaseAccountRequestDto that = (BaseAccountRequestDto) o;
        return  Objects.equals(getReferenceId(), that.getReferenceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReferenceId());
    }
}
