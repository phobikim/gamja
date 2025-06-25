package com.phobi.gamja.dto.contents;

import com.phobi.gamja.dto.battle.DropItemDto;
import com.phobi.gamja.entity.item.Item;
import lombok.Data;

import java.util.List;

@Data
public class MonsterDto {
    private Long id;
    private String name;
    private String desc;
    private String condition;
    private String rank;
    private String imagePath;
    private int monsterPower;
    private int monsterHp;
    private int monsterXp;

    private List<DropItemDto> dropItems;
}