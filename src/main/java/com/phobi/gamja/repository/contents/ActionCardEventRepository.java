package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.ActionCardEvent;
import com.phobi.gamja.entity.contents.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionCardEventRepository extends JpaRepository<ActionCardEvent, Long> {
    List<ActionCardEvent> findByActivityTypeAndRankAndIsEnabledTrue(ActivityType activityType, int rank);
}