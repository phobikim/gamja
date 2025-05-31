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
    private int fishing;
    private int mining;
    private int woodcutting;
    private int gathering;
    private int making;
    private List<ItemDto> equippedItems;
}