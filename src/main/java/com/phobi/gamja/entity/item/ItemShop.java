package com.phobi.gamja.entity.item;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "item_shop")
public class ItemShop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private int price;
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_type", nullable = false)
    private ShopType shopType; // NORMAL, EVENT, LIMITED

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ItemCategory category; // ADVENTURE, SKIN

    @Column(name = "target_id", nullable = false)
    private Long targetId; // item.id or skin_border.id

    @Column(name = "on_sale", nullable = false)
    private boolean onSale;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    public enum ShopType { NORMAL, EVENT, LIMITED }
    public enum ItemCategory { ADVENTURE, SKIN }
}