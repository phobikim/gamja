package com.example.gamja.dto;

import com.example.gamja.entity.Item;
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