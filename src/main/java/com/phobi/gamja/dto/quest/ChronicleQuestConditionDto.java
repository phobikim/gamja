package com.phobi.gamja.dto.quest;

import com.phobi.gamja.entity.user.CounterType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChronicleQuestConditionDto {
    private CounterType counterType;
    private Long targetId;
    private String targetName;

    private int requiredCount;
    private int currentCount;
    private int deliverableCount;

    private boolean achieved;
}