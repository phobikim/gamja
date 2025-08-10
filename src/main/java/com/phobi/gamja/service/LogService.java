package com.phobi.gamja.service;

import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.entity.user.UserDailyActionLog;
import com.phobi.gamja.repository.user.UserCounterDetailRepository;
import com.phobi.gamja.repository.user.UserDailyActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogService {
    private final UserCounterDetailRepository userCounterDetailRepository;
    private final UserDailyActionLogRepository userDailyActionLogRepository;
    @Transactional
    public void recordCounter(Long userId, CounterType counterType, Long targetId) {
        recordCounter(userId, counterType, targetId, 1);
    }

    @Transactional
    public void recordCounter(Long userId, CounterType counterType, Long targetId, int amount) {
        UserCounterDetail.PK pk = new UserCounterDetail.PK(userId, counterType, targetId);
        Optional<UserCounterDetail> optional = userCounterDetailRepository.findById(pk);

        if (optional.isPresent()) {
            UserCounterDetail detail = optional.get();
            detail.setCounterValue(detail.getCounterValue() + amount);
            userCounterDetailRepository.save(detail);
        } else {
            UserCounterDetail newDetail = UserCounterDetail.builder()
                    .userId(userId)
                    .counterType(counterType)
                    .targetId(targetId)
                    .counterValue(amount)
                    .build();
            userCounterDetailRepository.save(newDetail);
        }
    }

    @Transactional
    public boolean hasKilledBossToday(Long userId, Long monsterId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Integer sum = userDailyActionLogRepository
                .sumMonsterKillToday(userId, monsterId, today);
        return sum != null && sum > 0;
    }


}
