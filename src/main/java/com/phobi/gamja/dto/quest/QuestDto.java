package com.phobi.gamja.dto.quest;

import com.phobi.gamja.entity.quest.Quest.QuestType;
import com.phobi.gamja.entity.quest.Quest.QuestDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDto {
    private Long id;
    private String name;
    private String description;
    private QuestType type;
    private QuestDifficulty difficulty;
    private List<QuestConditionDto> conditions;
    private List<QuestRewardDto> rewards;
    private boolean achieved;
}