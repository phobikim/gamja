package com.phobi.gamja.dto.item;

import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemShop;
import com.phobi.gamja.entity.skin.SkinBorder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopItemDto {
    private String category;       // ADVENTURE or SKIN
    private Long targetId;         // item.id or skin_border.id
    private String name;
    private String description;
    private String iconPath;       // item.iconPath or skin.imagePath
    private int rank;              // 스킨은 0(또는 null)
    private int price;
    private Integer stock;
    private Integer availableQuantity;
    private boolean owned;

    // ADVENTURE용
    public static ShopItemDto fromAdventure(ItemShop s, Item item, int boughtToday) {
        int maxPerDay = s.getStock() != null ? s.getStock() : 99;
        int remaining = Math.max(0, maxPerDay - boughtToday);
        return ShopItemDto.builder()
                .category(s.getCategory().name())
                .targetId(s.getTargetId())
                .name(item.getName())
                .description(item.getDescription())
                .iconPath(item.getIconPath())
                .rank(item.getRank())
                .price(s.getPrice())
                .stock(s.getStock())
                .availableQuantity(remaining)
                .owned(false)
                .build();
    }

    // SKIN용
    public static ShopItemDto fromSkin(ItemShop s, SkinBorder skin, int boughtToday, boolean owned) {
        int maxPerDay = s.getStock() != null ? s.getStock() : 0;
        int remaining = Math.max(0, maxPerDay - boughtToday);
        return ShopItemDto.builder()
                .category(s.getCategory().name())
                .targetId(s.getTargetId())
                .name(skin.getName())
                .description(skin.getDescription())
                .iconPath(skin.getImageUrl()) // or imageUrl
                .rank(0)
                .price(s.getPrice())
                .stock(s.getStock())
                .availableQuantity(remaining)
                .owned(owned)
                .build();
    }
}