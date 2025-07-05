package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserChronicleComplete;
import com.phobi.gamja.entity.user.UserChronicleCompleteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChronicleCompleteRepository extends JpaRepository<UserChronicleComplete, UserChronicleCompleteId> {

    boolean existsByUserIdAndMapId(Long userId, Long mapId);
}