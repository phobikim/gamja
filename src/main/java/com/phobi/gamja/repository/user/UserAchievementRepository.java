package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.achievement.AchievementStatus;
import com.phobi.gamja.entity.user.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdAndEntryIdIn(Long userId, Collection<Long> entryIds);
    List<UserAchievement> findByUserIdAndStatus(Long userId, AchievementStatus status);

    Optional<UserAchievement> findByUserIdAndEntryId(Long userId, Long entryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ua from UserAchievement ua " +
            "where ua.userId = :userId and ua.entry.id = :entryId")
    Optional<UserAchievement> findByUserIdAndEntryIdForUpdate(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

}