package com.phobi.gamja.dto.battle;

import com.phobi.gamja.entity.item.Item;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DropItemDto {
    private Long itemId;
    private String name;
    private String iconPath;
    private Item.Rarity rarity;
    private Item.ItemType itemType;

    private float dropRate;     // 확률 (%)
    private int minCount;       // 최소 드랍 수
    private int maxCount;       // 최대 드랍 수
}