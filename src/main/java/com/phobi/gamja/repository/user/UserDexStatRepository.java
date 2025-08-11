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

    @Query("select uds.dex.id from UserDexStat uds " +
            "where uds.user.id = :userId and uds.affinity >= :minAffinity")
    List<Long> findDexIdsByUserIdAndAffinityAtLeast(Long userId, int minAffinity);
}