package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.achievement.AchievementStatus;

import java.time.LocalDateTime;

public class UserAchievementDto {
    public AchievementStatus status;
    public Integer progressCount;
    public LocalDateTime completedAt;
    public LocalDateTime rewardedAt;

    public UserAchievementDto() {}
}