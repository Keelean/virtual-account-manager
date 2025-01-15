package com.keelean.legacy.customeraccounts.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@SuperBuilder
public class FullVirtualAccountResponseDto extends BaseVirtualAccountResponseDto {
    private LocalDateTime expiryDate;
}
