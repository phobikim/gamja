package com.example.gamja.dto;

import com.example.gamja.entity.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ActionDto {
    private Long id;
    private String category;
    private int rank;
    private String displayName;
    private String description;
    private String iconPath;
    private int requiredLevel;
    private int level; // user_skill.level
    private int exp;   // user_skill.exp

    public static ActionDto of(Action action, int level, int exp) {
        return ActionDto.of(
                action.getId(),
                action.getCategory().name(),
                action.getRank(),
                action.getDisplayName(),
                action.getDescription(),
                action.getIconPath(),
                action.getRequiredLevel(),
                level,
                exp
        );
    }
}