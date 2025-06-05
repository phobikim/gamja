package com.phobi.gamja.dto.contents;

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
    private String rarity;
    private int level;
    private int xp;
    private int maxExp;

    private boolean selected; // 대표 감자인지 여부
}
