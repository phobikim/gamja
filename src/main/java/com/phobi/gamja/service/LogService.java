package com.phobi.gamja.service;

import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.repository.user.UserCounterDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogService {
    private final UserCounterDetailRepository userCounterDetailRepository;
    @Transactional
    public void recordCounter(Long userId, CounterType counterType, Long targetId) {
        UserCounterDetail.PK pk = new UserCounterDetail.PK(userId, counterType, targetId);
        Optional<UserCounterDetail> optional = userCounterDetailRepository.findById(pk);
        if (optional.isPresent()) {
            UserCounterDetail detail = optional.get();
            detail.setCounterValue(detail.getCounterValue() + 1);
            // updated_at은 DB에서 자동 갱신됨
            userCounterDetailRepository.save(detail);
        } else {
            UserCounterDetail newDetail = UserCounterDetail.builder()
                    .userId(userId)
                    .counterType(counterType)
                    .targetId(targetId)
                    .counterValue(1)
                    .build();
            userCounterDetailRepository.save(newDetail);
        }
    }
}
