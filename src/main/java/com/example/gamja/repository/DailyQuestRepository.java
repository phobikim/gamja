package com.example.gamja.repository;

import com.example.gamja.entity.DailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DailyQuestRepository extends JpaRepository<DailyQuest, Integer> {
    List<DailyQuest> findByDayAndIsActive(DayOfWeek day, boolean isActive);
}