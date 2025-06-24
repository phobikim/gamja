package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDailyQuestLog;
import com.phobi.gamja.entity.user.UserDailyQuestLogId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDailyQuestLogRepository extends JpaRepository<UserDailyQuestLog, UserDailyQuestLogId> {

    // 오늘 완료한 퀘스트 전체 조회
    List<UserDailyQuestLog> findByUserIdAndLogDate(Long userId, java.time.LocalDate logDate);

    // 특정 퀘스트 오늘 수행 여부 확인 (있으면 1건 나옴)
    boolean existsByUserIdAndQuestIdAndLogDate(Long userId, Long questId, java.time.LocalDate logDate);
}