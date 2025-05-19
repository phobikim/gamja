package com.example.gamja.repository;

import com.example.gamja.entity.DailyQuest;
import com.example.gamja.entity.UserResourceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface UserResourceLogRepository extends JpaRepository<UserResourceLog, Integer> {
    @Query("SELECT SUM(l.amount) FROM UserResourceLog l " +
            "WHERE l.userId = :userId AND l.resourceType = :resourceType " +
            "AND DATE(l.loggedAt) = :date")
    Integer sumAmountByUserIdAndResourceTypeAndDate(
            @Param("userId") Integer userId,
            @Param("resourceType") DailyQuest.ActionType resourceType,
            @Param("date") LocalDate date
    );
}