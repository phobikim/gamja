package com.phobi.gamja.dto.quest;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.quest.Quest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class ChronicleQuestDto {
    private Long id;
    private String name;
    private String description;
    private Long mapId;
    private Quest.QuestDifficulty difficulty;

    private boolean achieved;
    private boolean repeated;
    private boolean allowPartialDelivery;

    private List<ChronicleQuestConditionDto> conditions;
    private List<QuestRewardDto> rewards;
}