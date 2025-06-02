package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDexStatRepository extends JpaRepository<UserDexStat, UserDexStatId> {

    // 유저의 모든 캐릭터 stat 조회
    List<UserDexStat> findAllByIdUserId(Long userId);

    // 유저의 특정 캐릭터 stat 조회
    Optional<UserDexStat> findById(UserDexStatId id);
}