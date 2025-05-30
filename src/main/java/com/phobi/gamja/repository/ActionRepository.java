package com.phobi.gamja.repository;

import com.phobi.gamja.entity.Action;
import com.phobi.gamja.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {
    List<Action> findByCategoryAndIsEnabledOrderByRankAsc(ActivityType category, boolean isEnabled);
}