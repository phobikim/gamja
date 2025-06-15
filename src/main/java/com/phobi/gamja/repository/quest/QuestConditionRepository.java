package com.phobi.gamja.repository.quest;

import com.phobi.gamja.entity.quest.QuestCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestConditionRepository extends JpaRepository<QuestCondition, Long> {
    List<QuestCondition> findByQuestId(Long questId);

    // 여러 퀘스트 ID에 대한 조건 조회
    List<QuestCondition> findByQuestIdIn(List<Long> questIds);
}