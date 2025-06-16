package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.entity.user.UserInventoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInventoryRepository extends JpaRepository<UserInventory, UserInventoryId> {

    // 특정 유저의 모든 인벤토리 아이템 조회
    List<UserInventory> findByUserId(Long userId);
    Optional<UserInventory> findByUserIdAndItemId(Long userId, Long itemId);
    @Modifying
    @Query(value = "INSERT INTO user_inventory (user_id, item_id, quantity) VALUES (:userId, :itemId, :qty) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + :qty", nativeQuery = true)
    void upsertItem(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("qty") int qty);

}