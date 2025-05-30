package com.phobi.gamja.repository;

import com.phobi.gamja.entity.ActionDrop;
import com.phobi.gamja.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionDropRepository extends JpaRepository<ActionDrop, Long> {
    List<ActionDrop> findByActivityTypeAndSpotRank(ActivityType activityType, int spotRank);
}