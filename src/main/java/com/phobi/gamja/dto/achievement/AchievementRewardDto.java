package com.phobi.gamja.dto.achievement;

import com.phobi.gamja.entity.achievement.RewardType;

public class AchievementRewardDto {
    public Long id;
    public RewardType rewardType;
    public Integer amount;
    public String rewardKey;   // ex) GOLD, SLOT_UNLOCK code
    public Long rewardRefId;   // ex) titleId, itemId, skinId

    public AchievementRewardDto() {}
}