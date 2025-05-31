package com.phobi.gamja.dto.contents;

import com.phobi.gamja.entity.item.Item;
import lombok.Data;

import java.util.List;

@Data
public class MonsterDto {
    private Long id;
    private String name;
    private String desc;
    private String rank;
    private String imagePath;
    private int monsterPower;
    private int monsterHp;
    private int monsterXp;

    private List<Item> dropItems; // ✅ 이것만 있으면 충분!
}