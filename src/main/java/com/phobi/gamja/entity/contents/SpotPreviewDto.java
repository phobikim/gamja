package com.phobi.gamja.entity.contents;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpotPreviewDto {
    private String displayName;
    private String description;
    private String iconPath;
    private int requiredLevel;
    private int rank;
    private String category;

    public static SpotPreviewDto of(Action action) {
        return new SpotPreviewDto(
                action.getDisplayName(),
                action.getDescription(),
                action.getIconPath(),
                action.getRequiredLevel(),
                action.getRank(),
                action.getCategory().name()
        );
    }
}