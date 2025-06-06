package com.phobi.gamja.dto.contents;

import com.phobi.gamja.entity.contents.Dex;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DexOwnedDto {
    private Long dexId;
    private String dexImage;
    private String dexName;
    private String attribute;
    private Dex.DexRarity rarity;
    private int level;
    private int xp;
    private int maxExp;

    private boolean selected; // 대표 감자인지 여부
}
