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
public class BattleStatDetailDto {
    // stat detail
    private int fromUser;
    private int fromBase;
    private int fromEquip;
    private int fromTier;
}