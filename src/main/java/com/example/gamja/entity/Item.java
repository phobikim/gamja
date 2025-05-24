package com.example.gamja.entity;

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
    private EquipSlot equipSlot = EquipSlot.NONE;

    @Column(name = "icon_path", length = 255)
    private String iconPath;

    // enum 선언
    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }

    public enum ItemType {
        GATHER_MATERIAL, CRAFT_MATERIAL, COMPOSITE,
        EQUIP_GATHER, EQUIP_BATTLE, DROP
    }

    public enum EquipSlot {
        WEAPON, HELMET, ARMOR, GLOVES, SHOES, NONE
    }

}
