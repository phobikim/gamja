package com.example.gamja.repository;

import com.example.gamja.entity.ActionDrop;
import com.example.gamja.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionDropRepository extends JpaRepository<ActionDrop, Long> {
    List<ActionDrop> findByActivityTypeAndSpotRank(ActivityType activityType, int spotRank);
}