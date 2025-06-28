package com.phobi.gamja.service;

import com.phobi.gamja.entity.user.UserDailyActionLog;
import com.phobi.gamja.entity.user.UserDailyQuestLog;
import com.phobi.gamja.repository.user.UserDailyActionLogRepository;
import com.phobi.gamja.repository.user.UserDailyQuestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserDailyQuestLogRepository userDailyQuestLogRepository;
    private final UserDailyActionLogRepository userDailyActionLogRepository;

    private LocalDate today() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
    }

    // ✅ 퀘스트 수행 기록 (중복 방지)
    @Transactional
    public void recordDailyQuest(Long userId, Long questId) {
        boolean exists = userDailyQuestLogRepository
                .existsByUserIdAndQuestIdAndLogDate(userId, questId, today());

        if (!exists) {
            UserDailyQuestLog log = UserDailyQuestLog.builder()
                    .userId(userId)
                    .questId(questId)
                    .logDate(today())
                    .build();
            userDailyQuestLogRepository.save(log);
        }
    }

    // ✅ 몬스터 처치 기록 (+1 누적)
    @Transactional
    public void recordDailyMonster(Long userId, Long monsterId) {
        LocalDate date = today();

        Optional<UserDailyActionLog> optional = userDailyActionLogRepository
                .findByUserIdAndLogDateAndMonsterIdAndItemId(userId, date, monsterId, null); // ✅ itemId=null

        if (optional.isPresent()) {
            UserDailyActionLog log = optional.get();
            log.setCount(log.getCount() + 1);
            userDailyActionLogRepository.save(log);
        } else {
            UserDailyActionLog log = UserDailyActionLog.builder()
                    .userId(userId)
                    .logDate(date)
                    .monsterId(monsterId)
                    .itemId(null)
                    .count(1)
                    .build();
            userDailyActionLogRepository.save(log);
        }
    }

    // ✅ 아이템 제작 기록 (+1 누적)

    @Transactional
    public void recordDailyItem(Long userId, Long itemId) {
        LocalDate date = today();

        Optional<UserDailyActionLog> optional = userDailyActionLogRepository
                .findByUserIdAndLogDateAndMonsterIdAndItemId(userId, date, null, itemId); // ✅ monsterId=null

        if (optional.isPresent()) {
            UserDailyActionLog log = optional.get();
            log.setCount(log.getCount() + 1);
            userDailyActionLogRepository.save(log);
        } else {
            UserDailyActionLog log = UserDailyActionLog.builder()
                    .userId(userId)
                    .logDate(date)
                    .monsterId(null)
                    .itemId(itemId)
                    .count(1)
                    .build();
            userDailyActionLogRepository.save(log);
        }
    }
}
