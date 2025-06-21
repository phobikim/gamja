package com.phobi.gamja.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDexXpDto {
    private int level;
    private int xp;
    private int maxExp;
}
