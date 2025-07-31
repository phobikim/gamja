package com.phobi.gamja.dto.item;
import lombok.*;

import java.util.List;
import java.math.BigDecimal;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipBattleDto {
    private Long itemId;
    private String itemName;
    private String itemPath;
    private String description;
    private int quantity;

    // 기본 옵션
    private int bonusPower;
    private int bonusHp;
    private int bonusSpeed;
    private int enhancementLevel;

    private boolean equipped;

    // 특수 옵션
    private List<AlchemyOptionDto> alchemyOptions;
}