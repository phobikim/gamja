package com.phobi.gamja.dto.contents;

import com.phobi.gamja.dto.item.ItemRecipeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationDto {
    private String name;
    private String category;
    private String imagePath;

    private List<ItemRecipeDto> recipes;
}
