package com.phobi.gamja.entity.item;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "item_recipe")
@Data
public class ItemRecipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "station_category", nullable = false)
    private StationCategory stationCategory;

    @Column(name = "result_item_id", nullable = false)
    private Long resultItemId;

    @Column(name = "ingredient_item_id_1")
    private Long ingredientItemId1;

    @Column(name = "ingredient_quantity_1")
    private Integer ingredientQuantity1;

    @Column(name = "ingredient_item_id_2")
    private Long ingredientItemId2;

    @Column(name = "ingredient_quantity_2")
    private Integer ingredientQuantity2;

    @Column(name = "ingredient_item_id_3")
    private Long ingredientItemId3;

    @Column(name = "ingredient_quantity_3")
    private Integer ingredientQuantity3;

    @Column(name = "ingredient_item_id_4")
    private Long ingredientItemId4;

    @Column(name = "ingredient_quantity_4")
    private Integer ingredientQuantity4;

    public enum Grade {
        COMMON,     // 일반
        UNCOMMON,   // 고급
        RARE,       // 희귀
        EPIC,       // 영웅
        LEGENDARY;  // 전설

        @Override
        public String toString() {
            return name(); // 필요시 소문자로 변환도 가능: name().toLowerCase()
        }
    }

    public enum StationCategory {
        KITCHEN, WOODSHOP, FURNANCE, POTION, GATHER, BATTLE, LEATHER
    }
}
