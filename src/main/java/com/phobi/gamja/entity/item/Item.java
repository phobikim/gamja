package com.phobi.gamja.entity.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String condition;

    @Column(nullable = false)
    private int rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_slot", nullable = false)
    private EquipmentSlot equipSlot = EquipmentSlot.NONE;

    @Column(name = "icon_path", length = 255)
    private String iconPath;



    // enum 선언
    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

    }

    public enum ItemType {
        GATHER_MATERIAL, CRAFT_MATERIAL, COMPOSITE,
        EQUIP_GATHER, EQUIP_BATTLE, DROP, POTION
    }
    public enum EquipmentSlot {
        /* 전투용 */
        WEAPON, HELMET, ARMOR, PANTS, SHOES, RING, NECK, POTION,
        /* 생활용 */
        FISHING_ROD, AXE, PICKAXE, KNIFE,
        /* 기본값 */
        NONE
    }


}
