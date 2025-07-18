package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserEnhancement;
import com.phobi.gamja.entity.user.UserEnhancementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserEnhancementRepository extends JpaRepository<UserEnhancement, UserEnhancementId> {

    Optional<UserEnhancement> findByUserIdAndItemId(Long userId, Long itemId);

    List<UserEnhancement> findAllByUserId(Long userId);

    void deleteByUserIdAndItemId(Long userId, Long itemId);
}
