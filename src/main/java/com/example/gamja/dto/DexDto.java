package com.example.gamja.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DexDto {
    private Long id;
    private String name;
    private String description;
    private String image;
    private String rank;
    private String acquireCondition;
    private int acquiredCount;

    private boolean owned;
    private String acquiredAt;
}
