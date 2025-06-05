package com.phobi.gamja.dto.user;

import com.phobi.gamja.dto.item.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LifeStatDto {
    private LifeStatDetailDto fishing;
    private LifeStatDetailDto mining;
    private LifeStatDetailDto woodcutting;
    private LifeStatDetailDto gathering;
    private LifeStatDetailDto making;
    private List<ItemDto> equippedItems;
}