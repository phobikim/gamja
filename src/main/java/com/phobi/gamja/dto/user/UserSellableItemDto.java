package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.UserInventory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSellableItemDto {
    private Long targetId;
    private String name;
    private String description;
    private String iconPath;
    private int rank;
    private int quantity;
    private Integer sellPrice;

    public static UserSellableItemDto from(UserInventory inv) {
        Item item = inv.getItem();
        return UserSellableItemDto.builder()
                .targetId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .iconPath(item.getIconPath())
                .rank(item.getRank())
                .quantity(inv.getQuantity())
                .sellPrice(item.getPrice())
                .build();
    }
}