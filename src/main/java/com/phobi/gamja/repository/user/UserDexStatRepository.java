package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDexStatRepository extends JpaRepository<UserDexStat, UserDexStatId> {
    Optional<UserDexStat> findById(UserDexStatId id);

    Optional<UserDexStat> findByUserIdAndDexId(Long userId, Long dexId);

    List<UserDexStat> findByUser_Id(Long userId); // ← 이거 추가!
}