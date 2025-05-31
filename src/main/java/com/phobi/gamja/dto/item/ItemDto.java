package com.phobi.gamja.dto.item;

import com.phobi.gamja.entity.item.Item;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private int rank;
    private Item.Rarity rarity;
    private String itemType;
    private String equipSlot;
    private String iconPath;


}

