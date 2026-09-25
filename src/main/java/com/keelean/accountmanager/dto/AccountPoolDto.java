package com.keelean.accountmanager.dto;


import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.PoolType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class AccountPoolDto {

    @NotNull
    private AccountCapacity capacity;
    @Min(10)
    @Max(99)
    private int prefixSeries;
    @NotNull
    private PoolType poolType;
}
