package com.phobi.gamja.repository.user;

import com.phobi.gamja.dto.user.UserRankDto;
import com.phobi.gamja.entity.user.UserCorps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCorpsRepository extends JpaRepository<UserCorps, Long> {
    @Query("""
        SELECT new com.phobi.gamja.dto.user.UserRankDto(
            uc.userId,
            u.username,
            dtl.characterImage,
            uc.tier.id,
            uc.corpsLevel,
            uc.tier.name,
            uc.tier.iconPath,
            t.name,
            t.iconPath
        )
        FROM UserCorps uc
        JOIN User u ON uc.userId = u.id
        JOIN UserDtl dtl ON dtl.user = u
        LEFT JOIN UserTitle ut ON ut.id.userId = uc.userId AND ut.isEquipped = true
        LEFT JOIN Title t ON ut.title = t
        ORDER BY uc.tier.id DESC, uc.corpsLevel DESC, uc.corpsXp DESC
    """)
    List<UserRankDto> findTopRankList();
}