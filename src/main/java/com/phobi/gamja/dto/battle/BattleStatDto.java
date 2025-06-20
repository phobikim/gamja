package com.phobi.gamja.dto.battle;

import com.phobi.gamja.dto.item.ItemDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class BattleStatDto {
    // stat total
    private BattleStatDetailDto power;
    private BattleStatDetailDto hp;
    private BattleStatDetailDto speed;
    private List<ItemDto> equippedItems;

    public BattleStatDto(BattleStatDetailDto hp, BattleStatDetailDto power, BattleStatDetailDto speed, List<ItemDto> itemDtoList) {
        this.hp = hp;
        this.power = power;
        this.speed = speed;
        this.equippedItems = itemDtoList;
    }
}