package com.phobi.gamja.repository;

import com.phobi.gamja.entity.UserInventory;
import com.phobi.gamja.entity.UserInventoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInventoryRepository extends JpaRepository<UserInventory, UserInventoryId> {

    // 특정 유저의 모든 인벤토리 아이템 조회
    List<UserInventory> findByUserId(Long userId);
    Optional<UserInventory> findByUserIdAndItemId(Long userId, Long itemId);

}