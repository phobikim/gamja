package com.phobi.gamja.dto.item;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRecipeDto {
    private Long recipeId;
    private String recipeName;
    private String recipeDescription; // 이건 아이템 메타 테이블의 desc
    private String grade;

    private Long resultItemId;
    private String resultItemName;
    private String resultItemIcon;
    private int resultItemUserOwned; // 결과아이템 user 보유 수량
    private List<IngredientDto> ingredients;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientDto {
        private Long itemId;
        private String itemName;
        private String itemIcon;
        private int quantity;  // 제작 시 필요한 수량
        private int userOwned; // 유저가 보유 중인 수량
    }
}