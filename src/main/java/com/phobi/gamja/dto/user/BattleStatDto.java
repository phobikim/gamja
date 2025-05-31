package com.phobi.gamja.dto.user;

import com.phobi.gamja.dto.item.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleStatDto {
    private int totalHp;
    private int totalPower;
    private int totalSpeed;
    private List<ItemDto> equippedItems;
}