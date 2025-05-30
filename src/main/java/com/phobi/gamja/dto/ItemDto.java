package com.phobi.gamja.dto;

import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private int rank;
    private String rarity;
    private String itemType;
    private String equipSlot;
    private String iconPath;
}
