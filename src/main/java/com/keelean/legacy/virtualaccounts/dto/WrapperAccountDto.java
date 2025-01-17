package com.keelean.legacy.customeraccounts.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WrapperVirtualAccountDto<T extends BaseVirtualAccountRequestDto> {
    private List<T> requests;
}
