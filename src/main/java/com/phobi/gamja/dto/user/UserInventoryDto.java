package com.phobi.gamja.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInventoryDto {
    private Long itemId;
    private int quantity;
    private String updatedAt;

    // item 메타정보
    private String name;
    private String description;
    private int rank;
    private String rarity;
    private String itemType;
    private String equipSlot;
    private String iconPath;

}
