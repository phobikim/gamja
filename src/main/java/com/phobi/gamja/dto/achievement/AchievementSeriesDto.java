package com.phobi.gamja.dto.achievement;

import com.phobi.gamja.entity.achievement.AchievementCategory;

import java.util.List;

public class AchievementSeriesDto {
    public Long id;
    public String name;
    public String description;
    public String seriesKey;
    public AchievementCategory category;
    public boolean enabled;

    public List<AchievementEntryDto> entries;

    public AchievementSeriesDto() {}
}