package com.phobi.gamja.repository.quest;

import com.phobi.gamja.entity.quest.UserQuest;
import com.phobi.gamja.entity.quest.UserQuestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserQuestRepository extends JpaRepository<UserQuest, UserQuestId> {
    List<UserQuest> findByIdUserId(Long userId);
}
