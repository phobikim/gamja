package com.example.gamja.message;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CraftRequest {
    private Long resultItemId;
    private int resultQuantity;
    private List<CraftIngredient> ingredients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CraftIngredient {
        private Long itemId;
        private int quantity;
    }
}