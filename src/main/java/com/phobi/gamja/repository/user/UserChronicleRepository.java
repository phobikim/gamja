package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserChronicle;
import com.phobi.gamja.entity.user.UserChronicleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChronicleRepository extends JpaRepository<UserChronicle, UserChronicleId> {
    List<UserChronicle> findByUserId(Long userId);
    // 유저의 연대기 항목들 진행도
    List<UserChronicle> findByUserIdAndChronicleIdIn(Long userId, List<Long> chronicleIds);
}
