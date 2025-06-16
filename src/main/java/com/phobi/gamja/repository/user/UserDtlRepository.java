package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.User;
import com.phobi.gamja.entity.user.UserDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDtlRepository extends JpaRepository<UserDtl, Long> {
    Optional<UserDtl> findByUser(User user);

    @Query("SELECT ud.characterDexId FROM UserDtl ud WHERE ud.id = :userId")
    Long findCharacterDexIdByUserId(@Param("userId") Long userId);
}
