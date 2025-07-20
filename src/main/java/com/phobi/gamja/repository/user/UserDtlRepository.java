package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.User;
import com.phobi.gamja.entity.user.UserDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDtlRepository extends JpaRepository<UserDtl, Long> {
    Optional<UserDtl> findByUser(User user);

    @Query("SELECT ud.characterDexId FROM UserDtl ud WHERE ud.id = :userId")
    Long findCharacterDexIdByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserDtl u SET u.gold = u.gold + :amount WHERE u.id = :userId")
    void addGold(@Param("userId") Long userId, @Param("amount") Long amount);
}
