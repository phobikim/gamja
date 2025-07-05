package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.user.UserChronicle;
import com.phobi.gamja.entity.user.UserChronicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChronicleRepository extends JpaRepository<UserChronicle, UserChronicleId> {
    List<UserChronicle> findByUserId(Long userId);
    // 유저의 연대기 항목들 진행도
    List<UserChronicle> findByUserIdAndChronicleIdIn(Long userId, List<Long> chronicleIds);
    Optional<UserChronicle> findByUserIdAndChronicle(Long userId, Chronicle chronicle);

    @Query("SELECT uc.progressCount FROM UserChronicle uc " +
            "WHERE uc.userId = :userId AND uc.chronicle.targetType = 'ITEM' AND uc.chronicle.targetId = :itemId")
    Optional<Integer> findProgressCountByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

}
