package com.phobi.gamja.repository.achievement;

import com.phobi.gamja.entity.achievement.AchievementReward;
import com.phobi.gamja.entity.achievement.RewardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AchievementRewardRepository extends JpaRepository<AchievementReward, Long> {
    List<AchievementReward> findByEntryId(Long entryId);
    List<AchievementReward> findByEntryIdIn(Collection<Long> entryIds);
    List<AchievementReward> findByEntryIdAndRewardType(Long entryId, RewardType rewardType);
}
