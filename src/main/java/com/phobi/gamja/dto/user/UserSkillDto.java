package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.contents.SkillType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillDto {
    private SkillType skillType;
    private int level;
    private int xp;
    private int maxExp;
}