package com.phobi.gamja.repository.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemAlchemyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemAlchemyOptionRepository extends JpaRepository<ItemAlchemyOption, Long> {
    List<ItemAlchemyOption> findByRarityAndEquipSlot(Item.Rarity rarity, EquipmentSlot equipSlot);
}