package com.phobi.gamja.entity.item;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "item_shop")
public class ItemShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int price;

    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_type", nullable = false)
    private ShopType shopType;

    @Column(name = "on_sale", nullable = false)
    private boolean onSale;

    public enum ShopType {
        NORMAL,
        EVENT,
        LIMITED
    }
}