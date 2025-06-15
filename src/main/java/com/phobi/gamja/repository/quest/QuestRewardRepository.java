package com.phobi.gamja.repository.quest;

import com.phobi.gamja.entity.quest.QuestReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRewardRepository extends JpaRepository<QuestReward, Long> {
    List<QuestReward> findByQuestId(Long questId);
    // 여러 퀘스트 ID에 대한 보상 조회
    List<QuestReward> findByQuestIdIn(List<Long> questIds);
}