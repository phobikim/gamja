package com.phobi.gamja.repository.user;


import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.user.UserDex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDexRepository extends JpaRepository<UserDex, Long> {
    List<UserDex> findByUserId(Long userId);

    boolean existsByUserIdAndDexId(Long userId, Long dexId);

}
