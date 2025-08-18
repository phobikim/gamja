package com.phobi.gamja.dto.achievement;

import com.phobi.gamja.dto.user.UserAchievementDto;
import com.phobi.gamja.entity.achievement.RequirementType;

import java.time.LocalDateTime;
import java.util.List;

public class AchievementEntryDto {
    public Long id;
    public String description;
    public RequirementType requirementType;
    public Integer requirementValue;

    public Long characterId; // nullable
    public Long monsterId;   // nullable
    public Long itemId;      // nullable

    public Integer orderInSeries;
    public boolean enabled;

    public List<AchievementRewardDto> rewards;

    // 유저 진행 정보(없으면 null)
    public UserAchievementDto user;

    public AchievementEntryDto() {}
}