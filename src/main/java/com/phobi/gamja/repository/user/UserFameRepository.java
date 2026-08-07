package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserFame;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface UserFameRepository extends JpaRepository<UserFame, Long> {

    Optional<UserFame> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select uf from UserFame uf where uf.userId = :userId")
    Optional<UserFame> findByUserIdForUpdate(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserFame uf set uf.famePoint = uf.famePoint + :delta where uf.userId = :userId")
    int addFamePoint(@Param("userId") Long userId, @Param("delta") int delta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserFame uf set uf.xp = uf.xp + :delta where uf.userId = :userId")
    int addXp(@Param("userId") Long userId, @Param("delta") int delta);
}