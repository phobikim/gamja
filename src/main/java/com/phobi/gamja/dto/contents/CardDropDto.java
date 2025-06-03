package com.phobi.gamja.dto.contents;

import com.phobi.gamja.entity.contents.ActionCardEventDrop;
import com.phobi.gamja.entity.item.Item;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CardDropDto {
    private Long itemId;
    private String itemName;
    private String iconPath;
    private float dropRate;
    private int minQuantity;
    private int maxQuantity;

    public static CardDropDto of(ActionCardEventDrop drop) {
        Item item = drop.getItem();
        return CardDropDto.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .iconPath(item.getIconPath())
                .dropRate(drop.getDropRate())
                .minQuantity(drop.getMinQuantity())
                .maxQuantity(drop.getMaxQuantity())
                .build();
    }
}
