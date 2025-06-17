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

    // ✅ 현재 보유 수량 조회
    @Query("SELECT COALESCE(ui.quantity, 0) FROM UserInventory ui WHERE ui.userId = :userId AND ui.itemId = :itemId")
    Integer getQuantity(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // ✅ 수량 차감 (보유 수량보다 작을 때만 반영됨)
    @Modifying
    @Query("UPDATE UserInventory ui SET ui.quantity = ui.quantity - :amount " +
            "WHERE ui.userId = :userId AND ui.itemId = :itemId AND ui.quantity >= :amount")
    int consumeItem(@Param("userId") Long userId,
                    @Param("itemId") Long itemId,
                    @Param("amount") int amount);
}