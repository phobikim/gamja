package com.phobi.gamja.dto.battle;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResultDto {
    private Integer damage;
    private Integer heal;
    private Integer buff;
}