package com.phobi.gamja.repository.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByIdInAndItemType(List<Long> ids, Item.ItemType itemType);
    List<Item> findByIdInAndEquipSlot(List<Long> ids, EquipmentSlot equipSlot);
    List<Item> findByIdInAndItemTypeAndEquipSlot(List<Long> ids, Item.ItemType type, EquipmentSlot slot);

}
