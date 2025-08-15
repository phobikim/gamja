package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserSkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkinRepository extends JpaRepository<UserSkin, Long> {
    // 배경 보유 항목만 조회 (skinBackground not null)
    List<UserSkin> findByUserIdAndSkinBackgroundIsNotNull(Long userId);

    // 테두리 보유 항목만 조회 (skinBorder not null) — 추후 테두리 기능에 사용
    List<UserSkin> findByUserIdAndSkinBorderIsNotNull(Long userId);
    boolean existsByUserIdAndSkinBorder_Id(Long userId, Long skinBorderId);
    Optional<UserSkin> findByUserIdAndSkinBorder_Id(Long userId, Long skinBorderId);
    @Query("select us.skinBorder.id from UserSkin us where us.userId = :userId")
    List<Long> findOwnedSkinBorderIdsByUserId(@Param("userId") Long userId);

}