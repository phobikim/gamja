package com.phobi.gamja.entity.contents;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExplorationSession {
    private Long userId;
    private int hp;
    private int stage;
    private List<Long> usedCardIds;
    private List<ExplorationReward> rewards;
    private List<ActionCardEvent> currentChoices;
}