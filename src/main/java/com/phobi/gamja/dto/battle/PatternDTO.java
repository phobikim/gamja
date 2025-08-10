package com.phobi.gamja.dto.battle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternDTO {
    private Long id;
    private Long monsterId;
    private int phase;
    private int phaseOrder;
    private String dialogue;
    private String type;        // enum name
    private int value;
    private boolean enabled;
    private Integer cooldown;   // nullable
}
