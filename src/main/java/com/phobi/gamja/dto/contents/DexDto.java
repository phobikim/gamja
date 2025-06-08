package com.phobi.gamja.dto.contents;

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
    private String rarity;
    private String condition;
    private String attribute;
    private String attributeIconPath;
    private boolean owned;
    private String acquiredAt;
    private boolean userFlag;
}
