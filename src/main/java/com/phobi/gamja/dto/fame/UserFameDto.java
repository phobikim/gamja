package com.phobi.gamja.dto.fame;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserFameDto {
    private Integer fameId;      // fameTier.fameId
    private String fameName;     // fameTier.name
    private String fameDesc;     // fameTier.description

    private Integer fameLevel;
    private Integer xp;
    private Integer maxXp;
    private Integer famePoint;
}