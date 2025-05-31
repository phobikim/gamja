package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {
    // userId로 조회 (PK)
    UserStat findByUserId(Long userId);
}
