package com.phobi.gamja.repository.quest;

import com.phobi.gamja.entity.quest.UserQuest;
import com.phobi.gamja.entity.quest.UserQuestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuestRepository extends JpaRepository<UserQuest, UserQuestId> {
    List<UserQuest> findByIdUserId(Long userId);
    @Query("SELECT uq.id.questId FROM UserQuest uq WHERE uq.id.userId = :userId AND uq.completed = true")
    List<Long> findCompletedQuestIds(@Param("userId") Long userId);
}
