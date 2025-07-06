package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDailyActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActionLogRepository extends JpaRepository<UserDailyActionLog, Long> {

    Optional<UserDailyActionLog> findByUserIdAndLogDateAndMonsterIdAndItemId(
            Long userId, LocalDate logDate, Long monsterId, Long itemId);

    List<UserDailyActionLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    @Query("SELECT SUM(l.count) FROM UserDailyActionLog l " +
            "WHERE l.userId = :userId AND l.monster.id = :monsterId AND l.logDate = :logDate")
    Integer sumMonsterKillToday(@Param("userId") Long userId,
                                @Param("monsterId") Long monsterId,
                                @Param("logDate") LocalDate logDate);
}