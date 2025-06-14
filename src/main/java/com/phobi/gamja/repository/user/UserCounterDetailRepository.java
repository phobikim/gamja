package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.entity.user.CounterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCounterDetailRepository extends JpaRepository<UserCounterDetail, UserCounterDetail.PK> {

    List<UserCounterDetail> findAllByUserIdAndCounterType(Long userId, CounterType counterType);

    boolean existsByUserIdAndCounterTypeAndTargetId(Long userId, CounterType counterType, Long targetId);

    List<UserCounterDetail> findByUserId(Long userId);
}