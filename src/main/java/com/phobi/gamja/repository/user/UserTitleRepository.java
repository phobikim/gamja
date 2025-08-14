package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserTitleRepository extends JpaRepository<UserTitle, UserTitleId> {

    List<UserTitle> findByIdUserId(Long userId);
    Optional<UserTitle> findByIdUserIdAndIsEquippedTrue(Long userId);
    boolean existsById(UserTitleId id);

    @Query("SELECT COUNT(u) > 0 FROM UserTitle u WHERE u.id.userId = :userId AND u.id.titleId = :titleId AND u.isEquipped = true")
    boolean existsEquippedTitle(@Param("userId") Long userId, @Param("titleId") Long titleId);

}
