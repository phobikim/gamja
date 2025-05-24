package com.example.gamja.repository;

import com.example.gamja.entity.Action;
import com.example.gamja.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {
    List<Action> findByCategoryAndIsEnabledOrderByRankAsc(ActivityType category, boolean isEnabled);
}