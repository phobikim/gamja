package com.phobi.gamja.dto.item;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipPotionDto {
    private Long itemId;
    private String itemName;
    private String itemPath;
    private String description;
    private int quantity;

    private int bonusPower;  // 공격력 회복
    private int bonusHp;     // 체력 회복
}