package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserTitleRepository extends JpaRepository<UserTitle, UserTitleId> {

    // EmbeddedId.userId 로 조회
    List<UserTitle> findByIdUserId(Long userId);

    // 장착중인 칭호 1개 조회
    Optional<UserTitle> findByIdUserIdAndIsEquippedTrue(Long userId);

    // 해당 칭호 보유 여부
    boolean existsById(UserTitleId id);
}
