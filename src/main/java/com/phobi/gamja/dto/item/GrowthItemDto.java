package com.phobi.gamja.dto.item;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthItemDto {
    private Long itemId;
    private String name;
    private String description;
    private String iconPath;
    private int quantity;
    private int bonusExp;
}
