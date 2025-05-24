package com.example.gamja.repository;

import com.example.gamja.entity.UserDtl;
import com.example.gamja.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {

    // 특정 유저의 모든 인벤토리 아이템 조회
    List<UserInventory> findByUserId(Long userId);
    Optional<UserInventory> findByUserIdAndItemId(Long userId, Long itemId);

}