package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDailyActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActionLogRepository extends JpaRepository<UserDailyActionLog, Long> {

    Optional<UserDailyActionLog> findByUserIdAndLogDateAndMonsterIdAndItemId(
            Long userId, LocalDate logDate, Long monsterId, Long itemId);

    List<UserDailyActionLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}