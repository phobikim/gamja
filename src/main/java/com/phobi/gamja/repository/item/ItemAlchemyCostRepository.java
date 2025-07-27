package com.phobi.gamja.repository.item;


import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemAlchemyCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemAlchemyCostRepository extends JpaRepository<ItemAlchemyCost, Long> {
    Optional<ItemAlchemyCost> findByRarityAndEquipSlot(Item.Rarity rarity, EquipmentSlot equipSlot);
}
