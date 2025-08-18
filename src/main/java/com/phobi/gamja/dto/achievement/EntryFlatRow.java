package com.phobi.gamja.dto.achievement;

import com.phobi.gamja.entity.achievement.AchievementStatus;
import com.phobi.gamja.entity.achievement.RequirementType;
import com.phobi.gamja.entity.achievement.RewardType;

import java.time.LocalDateTime;

public class EntryFlatRow {
    public Long entryId;
    public String description;
    public RequirementType requirementType;
    public Integer requirementValue;
    public Long characterId;
    public Long monsterId;
    public Long itemId;
    public Integer orderInSeries;
    public Boolean entryEnabled;

    public Long rewardId;
    public RewardType rewardType;
    public Integer rewardAmount;
    public String rewardKey;
    public Long rewardRefId;

    public AchievementStatus userStatus;
    public Integer userProgressCount;
    public LocalDateTime userCompletedAt;
    public LocalDateTime userRewardedAt;

    public EntryFlatRow(Long entryId, String description, RequirementType requirementType, Integer requirementValue,
                        Long characterId, Long monsterId, Long itemId, Integer orderInSeries, Boolean entryEnabled,
                        Long rewardId, RewardType rewardType, Integer rewardAmount, String rewardKey, Long rewardRefId,
                        AchievementStatus userStatus, Integer userProgressCount,
                        LocalDateTime userCompletedAt, LocalDateTime userRewardedAt) {
        this.entryId = entryId;
        this.description = description;
        this.requirementType = requirementType;
        this.requirementValue = requirementValue;
        this.characterId = characterId;
        this.monsterId = monsterId;
        this.itemId = itemId;
        this.orderInSeries = orderInSeries;
        this.entryEnabled = entryEnabled;
        this.rewardId = rewardId;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.rewardKey = rewardKey;
        this.rewardRefId = rewardRefId;
        this.userStatus = userStatus;
        this.userProgressCount = userProgressCount;
        this.userCompletedAt = userCompletedAt;
        this.userRewardedAt = userRewardedAt;
    }
}
