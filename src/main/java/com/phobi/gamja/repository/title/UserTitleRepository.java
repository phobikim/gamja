package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTitleRepository extends JpaRepository<UserTitle, UserTitleId> {
    List<UserTitle> findByUserId(Long userId);
    UserTitle findByUserIdAndIsEquippedTrue(Long userId);
}
