package com.phobi.gamja.entity.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "item_enhance_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEnhanceMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Item.Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_slot", nullable = false)
    private EquipmentSlot equipSlot;

    @Column(name = "enhancement_level", nullable = false)
    private int enhancementLevel;

    @Column(name = "gold_cost", nullable = false)
    private int goldCost;

    @Column(name = "success_rate", nullable = false)
    private int successRate;

    @Column(name = "bonus_power", nullable = false)
    private int bonusPower;

    @Column(name = "bonus_hp", nullable = false)
    private int bonusHp;

    @Column(name = "bonus_speed", nullable = false)
    private int bonusSpeed;

    @Column(name = "material_item_id_1")
    private Long materialItemId1;

    @Column(name = "material_quantity_1")
    private int materialQuantity1;

    @Column(name = "material_item_id_2")
    private Long materialItemId2;

    @Column(name = "material_quantity_2")
    private int materialQuantity2;

    @Column(name = "material_item_id_3")
    private Long materialItemId3;

    @Column(name = "material_quantity_3")
    private int materialQuantity3;

    @Column(name = "material_item_id_4")
    private Long materialItemId4;

    @Column(name = "material_quantity_4")
    private int materialQuantity4;
}
