package com.phobi.gamja.entity.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "item_alchemy_cost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAlchemyCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Item.Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_slot", nullable = false)
    private EquipmentSlot equipSlot;

    @Column(name = "gold_cost")
    private Integer goldCost;

    @Column(name = "material_item_id_1")
    private Long materialItemId1;

    @Column(name = "material_quantity_1")
    private Integer materialQuantity1;

    @Column(name = "material_item_id_2")
    private Long materialItemId2;

    @Column(name = "material_quantity_2")
    private Integer materialQuantity2;

    @Column(name = "material_item_id_3")
    private Long materialItemId3;

    @Column(name = "material_quantity_3")
    private Integer materialQuantity3;
}
