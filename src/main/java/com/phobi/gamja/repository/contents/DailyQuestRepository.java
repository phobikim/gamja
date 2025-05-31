package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.DailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DailyQuestRepository extends JpaRepository<DailyQuest, Integer> {
    List<DailyQuest> findByDayAndIsActive(DayOfWeek day, boolean isActive);
}