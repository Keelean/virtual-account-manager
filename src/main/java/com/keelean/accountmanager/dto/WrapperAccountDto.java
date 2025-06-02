package com.keelean.accountmanager.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WrapperAccountDto<T extends BaseAccountRequestDto> {
    private List<T> requests;
}
