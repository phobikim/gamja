package com.phobi.gamja.repository.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.repository.user.UserSellableItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByIdInAndItemType(List<Long> ids, Item.ItemType itemType);
    List<Item> findByIdInAndEquipSlot(List<Long> ids, EquipmentSlot equipSlot);
    List<Item> findByIdInAndItemTypeAndEquipSlot(List<Long> ids, Item.ItemType type, EquipmentSlot slot);

    List<Item> findByItemType(Item.ItemType itemType);


    @Query(value = """
        SELECT 
            i.id AS itemId,
            i.name AS name,
            i.description AS description,
            i.icon_path AS iconPath,
            i.rank AS rank,
            i.price AS sellPrice,
            IFNULL(ui.quantity, 0) AS quantity
        FROM item i
        LEFT JOIN user_inventory ui
            ON i.id = ui.item_id AND ui.user_id = :userId
        WHERE i.price > 0
        ORDER BY i.price ASC
    """, nativeQuery = true)
    List<UserSellableItemProjection> findSellableItemsWithUserQuantity(@Param("userId") Long userId);
}
