package com.phobi.gamja.repository.user;


import com.phobi.gamja.entity.user.UserDex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDexRepository extends JpaRepository<UserDex, Long> {
    List<UserDex> findByUserId(Long userId);

    boolean existsByUserIdAndDexId(Long userId, Long dexId);
}
