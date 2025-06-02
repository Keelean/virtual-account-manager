package com.keelean.accountmanager.entity;


import com.keelean.accountmanager.enums.PoolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class AssignedPoolSequence {
    private String poolSequence;
    private PoolType poolType;
}
