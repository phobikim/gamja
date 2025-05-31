package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.ActionDrop;
import com.phobi.gamja.entity.contents.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionDropRepository extends JpaRepository<ActionDrop, Long> {
    List<ActionDrop> findByActivityTypeAndSpotRank(ActivityType activityType, int spotRank);
}