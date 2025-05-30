package com.phobi.gamja.repository;

import com.phobi.gamja.entity.DailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DailyQuestRepository extends JpaRepository<DailyQuest, Integer> {
    List<DailyQuest> findByDayAndIsActive(DayOfWeek day, boolean isActive);
}