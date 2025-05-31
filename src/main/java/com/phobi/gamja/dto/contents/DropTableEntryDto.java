package com.phobi.gamja.dto.contents;

import com.phobi.gamja.entity.contents.ActionDrop;
import com.phobi.gamja.entity.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DropTableEntryDto {
    private Long itemId;
    private String name;
    private String iconPath;
    private float dropRate;
    private float expReward;
    private int minQuantity;
    private int maxQuantity;
    private int requiredSkillLevel;
    private float rarityBoost;
    private String timeCondition;
    private String season;

    public static DropTableEntryDto of(ActionDrop drop, Item item) {
        return new DropTableEntryDto(
                item.getId(),
                item.getName(),
                item.getIconPath(),
                drop.getDropRate(),
                drop.getExpReward(),
                drop.getMinQuantity(),
                drop.getMaxQuantity(),
                drop.getRequiredSkillLevel(),
                drop.getRarityBoost(),
                drop.getTimeCondition().name(),
                drop.getSeason().name()
        );
    }
}
