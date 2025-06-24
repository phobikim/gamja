package com.phobi.gamja.entity.user;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyQuestLogId implements Serializable {
    private Long userId;
    private Long questId;
    private LocalDate logDate;
}
