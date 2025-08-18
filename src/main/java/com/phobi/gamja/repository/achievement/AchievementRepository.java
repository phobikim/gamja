package com.phobi.gamja.repository.achievement;

import com.phobi.gamja.entity.achievement.Achievement;
import com.phobi.gamja.entity.achievement.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findBySeriesKey(String seriesKey);
    Optional<Achievement> findBySeriesKeyAndEnabledTrue(String seriesKey);
    List<Achievement> findByCategoryAndEnabledTrueOrderByCreatedAtDesc(AchievementCategory category);
}