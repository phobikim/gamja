package com.phobi.gamja.dto.item;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipGatherDto {
    private Long itemId;
    private String itemName;
    private String itemPath;
    private String description;
    private int quantity;

    // 생활 스킬 관련 보너스
    private Integer bonusSkillFish;
    private Integer bonusSkillGathering;
    private Integer bonusSkillWoodCutting;
    private Integer bonusSkillMining;
    private Integer bonusSkillMaking;
}