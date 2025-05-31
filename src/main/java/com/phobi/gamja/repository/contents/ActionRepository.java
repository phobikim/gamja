package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.Action;
import com.phobi.gamja.entity.contents.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {
    List<Action> findByCategoryAndIsEnabledOrderByRankAsc(ActivityType category, boolean isEnabled);
}