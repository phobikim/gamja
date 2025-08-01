package com.phobi.gamja.entity.battle;

import com.phobi.gamja.dto.battle.DropItemDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BattleSession {
    private Long userId;

    // 유저 상태
    private int playerHp;
    private int playerMaxHp;
    private int playerPower;
    private int playerSpeed;
    private int playerXp;
    private int playerLevel;
    private boolean isPlayerTurn = true;

    private int playerPotionHp;
    private int playerPotionPower;
    private int playerPotionQuantity;
    private int playerBasePower;  // 원래 공격력
    private boolean bonusApplied; // 포션 공격력 버프 중복 방지

    // 유저 특수 옵션
    private double playerSpeedCritRate;
    private double critRate;
    private double critDmg;
    private double expGain;
    private double goldGain;

    // 몬스터 상태
    private Long monsterId;
    private String monsterName;
    private int monsterHp;
    private int monsterMaxHp;
    private int monsterPower;
    private int monsterXp;
    private List<DropItemDto> monsterDrops;
}