package com.phobi.gamja.dto.item;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipItemDto {
    private Long itemId;
    private String itemName;
    private String itemPath;
    private String description;
    private Integer bonusPower;
    private Integer bonusHp;
    private Integer bonusSpeed;
    private int quantity;
    private Integer durationTurns;
    private Integer bonusSkillFish;
    private Integer bonusSkillMining;
    private Integer bonusSkillWoodCutting;
    private Integer bonusSkillGathering;
    private Integer bonusSkillMaking;
    private Integer enhancementLevel;
}