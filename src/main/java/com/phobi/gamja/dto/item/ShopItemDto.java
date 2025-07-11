package com.phobi.gamja.dto.item;

import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemShop;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopItemDto {
    private Long itemId;
    private String name;
    private String description;
    private String iconPath;
    private int rank;
    private int price;
    private int stock;
    private Integer availableQuantity;

    public static ShopItemDto from(ItemShop shopItem, int boughtToday) {
        int maxPerDay = shopItem.getStock() != null ? shopItem.getStock() : 99;
        int remaining = Math.max(0, maxPerDay - boughtToday);

        Item item = shopItem.getItem();
        return ShopItemDto.builder()
                .itemId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .iconPath(item.getIconPath())
                .rank(item.getRank())
                .price(shopItem.getPrice())
                .stock(shopItem.getStock())
                .availableQuantity(remaining)
                .build();
    }
}