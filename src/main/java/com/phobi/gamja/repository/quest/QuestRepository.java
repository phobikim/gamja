package com.phobi.gamja.repository.quest;

import com.phobi.gamja.entity.quest.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByType(Quest.QuestType questType);
}