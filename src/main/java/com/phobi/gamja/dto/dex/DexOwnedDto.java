package com.phobi.gamja.dto.dex;

import com.phobi.gamja.entity.dex.Dex;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DexOwnedDto {
    private Long dexId;
    private String dexImage;
    private String dexName;
    private String rarity;
    private int level;
    private int xp;
    private int maxExp;
    private int affinity;
    private int power;
    private int hp;
    private int speed;
    private String attribute;           // 속성 이름
    private String attributeIconPath;   // 속성 아이콘 경로

    private boolean selected; // 대표 감자인지 여부
}
