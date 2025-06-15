package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserTitleRepository extends JpaRepository<UserTitle, UserTitleId> {

    List<UserTitle> findByIdUserId(Long userId);
    Optional<UserTitle> findByIdUserIdAndIsEquippedTrue(Long userId);
    boolean existsById(UserTitleId id);
}
