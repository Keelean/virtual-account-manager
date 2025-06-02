package com.keelean.accountmanager.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@SuperBuilder
public class FullAccountResponseDto extends BaseAccountResponseDto {
    private LocalDateTime expiryDate;
}
