package com.phobi.gamja.repository.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemEnhanceMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemEnhanceMaterialRepository extends JpaRepository<ItemEnhanceMaterial, Long> {

    // 슬롯별 조회: 공용 + 특정 슬롯 포함
    Optional<ItemEnhanceMaterial> findByRarityAndEquipSlotAndEnhancementLevel(Item.Rarity rarity, EquipmentSlot slot, int level);

    List<ItemEnhanceMaterial> findByRarityAndEnhancementLevel(Item.Rarity rarity, int level);

    @Query("SELECT m FROM ItemEnhanceMaterial m " +
            "WHERE m.rarity = :rarity " +
            "AND m.enhancementLevel = :level " +
            "AND (m.equipSlot = :slot OR m.equipSlot = com.phobi.gamja.dto.item.EquipmentSlot.ALL)")
    Optional<ItemEnhanceMaterial> findWithAllSlotIncluded(
            @Param("rarity") Item.Rarity rarity,
            @Param("slot") EquipmentSlot slot,
            @Param("level") int level);
}