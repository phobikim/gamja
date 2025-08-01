package com.phobi.gamja.dto.battle;

import com.phobi.gamja.dto.item.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleStatDto {
    // stat total
    private BattleStatDetailDto power;
    private BattleStatDetailDto hp;
    private BattleStatDetailDto speed;
    private List<ItemDto> equippedItems;
    private double critRate;
    private double critDmg;
    private double expGain;
    private double goldGain;

    public BattleStatDto(BattleStatDetailDto hp, BattleStatDetailDto power, BattleStatDetailDto speed, List<ItemDto> itemDtoList) {
        this.hp = hp;
        this.power = power;
        this.speed = speed;
        this.equippedItems = itemDtoList;
    }
}