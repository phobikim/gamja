package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.user.CounterType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCounterDetailDto {
    private Long userId;
    private CounterType counterType;
    private Long targetId;
    private Integer counterValue;
}