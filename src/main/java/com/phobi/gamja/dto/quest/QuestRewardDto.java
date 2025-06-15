package com.phobi.gamja.dto.quest;

import com.phobi.gamja.entity.quest.QuestReward.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestRewardDto {
    private RewardType rewardType;
    private Long itemId;
    private int amount;
}