package com.keelean.legacy.customeraccounts.dto;

import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter
@Getter
@SuperBuilder
@ToString
public class BaseVirtualAccountRequestDto implements Serializable {
    @NotNull
    private String partnerId;
    @NotNull
    private String referenceId;
    private String accountId;
    private String invoiceRef;
    private VirtualAccountMode mode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseVirtualAccountRequestDto)) return false;
        BaseVirtualAccountRequestDto that = (BaseVirtualAccountRequestDto) o;
        return  Objects.equals(getReferenceId(), that.getReferenceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReferenceId());
    }
}
