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
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class AccountPoolDto {

    @NotNull
    private AccountCapacity capacity;
    @Size(min = 10)
    private int prefixSeries;
    @NotNull
    private PoolType poolType;
}
