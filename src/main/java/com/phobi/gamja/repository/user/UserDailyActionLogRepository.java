package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDailyActionLog;
import com.phobi.gamja.entity.user.UserDailyActionLogId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDailyActionLogRepository extends JpaRepository<UserDailyActionLog, UserDailyActionLogId> {

    // 특정 몬스터의 오늘 수행 로그 가져오기
    Optional<UserDailyActionLog> findByUserIdAndLogDateAndMonsterId(Long userId, java.time.LocalDate logDate, Long monsterId);

    // 특정 아이템 제작의 오늘 수행 로그 가져오기
    Optional<UserDailyActionLog> findByUserIdAndLogDateAndItemId(Long userId, java.time.LocalDate logDate, Long itemId);

    // 오늘 전체 활동 로그
    List<UserDailyActionLog> findByUserIdAndLogDate(Long userId, java.time.LocalDate logDate);
}