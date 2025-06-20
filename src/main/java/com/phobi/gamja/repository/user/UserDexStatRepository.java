package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserDexStatRepository extends JpaRepository<UserDexStat, UserDexStatId> {
    Optional<UserDexStat> findById(UserDexStatId id);

    Optional<UserDexStat> findByUserIdAndDexId(Long userId, Long dexId);

    List<UserDexStat> findByUser_Id(Long userId);

    @Modifying
    @Query("UPDATE UserDexStat uds SET uds.xp = uds.xp + :amount WHERE uds.id.userId = :userId AND uds.id.dexId = :dexId")
    void addXp(@Param("userId") Long userId, @Param("dexId") Long dexId, @Param("amount") int amount);

    @Modifying
    @Transactional
    @Query(value = """
    INSERT IGNORE INTO user_dex_stat (user_id, dex_id, level, xp, max_exp, power, hp, speed)
    VALUES (:userId, :dexId, 1, 0, 100, 0, 0, 0)
    """, nativeQuery = true)
    void insertIfNotExists(@Param("userId") Long userId, @Param("dexId") Long dexId);


}