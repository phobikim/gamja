package com.phobi.gamja.entity.user;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyActionLogId implements Serializable {
    private Long userId;
    private LocalDate logDate;
    private Long monsterId;
    private Long itemId;
}