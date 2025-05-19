package com.example.gamja.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestListResponseDto {
    private Integer questId;
    private String title;
    private String description;
    private String action;
    private Integer goalCount;
    private String rewardType;
    private Integer rewardValue;
    private String difficulty;
    private Boolean isCompleted;
}