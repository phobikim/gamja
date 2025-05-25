package com.example.gamja.repository;

import com.example.gamja.entity.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {
    // userId로 조회 (PK)
    UserStat findByUserId(Long userId);
}
