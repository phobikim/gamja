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

    public static ShopItemDto from(ItemShop shop) {
        Item item = shop.getItem();
        return ShopItemDto.builder()
                .itemId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .iconPath(item.getIconPath())
                .rank(item.getRank())
                .price(shop.getPrice())
                .stock(shop.getStock())
                .build();
    }
}