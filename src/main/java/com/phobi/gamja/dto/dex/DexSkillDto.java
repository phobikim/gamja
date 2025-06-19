package com.phobi.gamja.dto.dex;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DexSkillDto {
    private Long id;
    private String name;
    private String description;
    private float powerRatio;
    private String target;
    private String effect;
    private Integer effectValue;
    private int mpCost;
    private int cooldown;
    private List<String> images;
}