package com.phobi.gamja.dto.quest;

import com.phobi.gamja.entity.user.CounterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestConditionDto {
    private CounterType counterType;
    private Long targetId;
    private String targetName;
    private int requiredCount;
    private int currentCount;
    private boolean achieved;
}
