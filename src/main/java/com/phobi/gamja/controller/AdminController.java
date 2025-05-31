package com.phobi.gamja.controller;

import com.phobi.gamja.dto.item.ItemRecipeDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemRecipe;
import com.phobi.gamja.repository.item.ItemRecipeRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ItemRepository itemRepository;
    private final ItemRecipeRepository recipeRepository;

    // 1. 모든 아이템 리스트 조회
    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // 2. 아이템 저장
    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    // 3. 아이템 수정
    @PutMapping("/items/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item update) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(update.getName());
                    item.setDescription(update.getDescription());
                    item.setRank(update.getRank());
                    item.setRarity(update.getRarity());
                    item.setItemType(update.getItemType());
                    item.setEquipSlot(update.getEquipSlot());
                    item.setIconPath(update.getIconPath());
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    // 4. 레시피 목록 조회 (RecipeDto 변환 포함)
    @GetMapping("/recipes")
    public List<ItemRecipeDto> getAllRecipes() {
        List<Item> items = itemRepository.findAll();
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, i -> i));

        return recipeRepository.findAll().stream().map(recipe -> {
            Item result = itemMap.get(recipe.getResultItemId());

            List<ItemRecipeDto.IngredientDto> ingredients = new ArrayList<>();
            addIngredient(ingredients, itemMap, recipe.getIngredientItemId1(), recipe.getIngredientQuantity1());
            addIngredient(ingredients, itemMap, recipe.getIngredientItemId2(), recipe.getIngredientQuantity2());
            addIngredient(ingredients, itemMap, recipe.getIngredientItemId3(), recipe.getIngredientQuantity3());
            addIngredient(ingredients, itemMap, recipe.getIngredientItemId4(), recipe.getIngredientQuantity4());

            return ItemRecipeDto.builder()
                    .recipeId(recipe.getId())
                    .recipeName(recipe.getName())
                    .recipeDescription("") // 필요 시 설정
                    .grade(recipe.getGrade().name())
                    .resultItemId(result.getId())
                    .resultItemName(result.getName())
                    .resultItemIcon(result.getIconPath())
                    .resultItemUserOwned(0) // 관리자 페이지에서는 필요 없음
                    .ingredients(ingredients)
                    .build();
        }).collect(Collectors.toList());
    }

    private void addIngredient(List<ItemRecipeDto.IngredientDto> list, Map<Long, Item> itemMap, Long id, Integer qty) {
        if (id != null && qty != null) {
            Item item = itemMap.get(id);
            if (item != null) {
                list.add(ItemRecipeDto.IngredientDto.builder()
                        .itemId(item.getId())
                        .itemName(item.getName())
                        .itemIcon(item.getIconPath())
                        .quantity(qty)
                        .userOwned(0)
                        .build());
            }
        }
    }

    // 5. 레시피 등록
    @PostMapping("/recipes")
    public ItemRecipe createRecipe(@RequestBody ItemRecipe recipe) {
        return recipeRepository.save(recipe);
    }

    // 6. 레시피 수정
    @PutMapping("/recipes/{id}")
    public ItemRecipe updateRecipe(@PathVariable Long id, @RequestBody ItemRecipe update) {
        return recipeRepository.findById(id)
                .map(r -> {
                    r.setName(update.getName());
                    r.setGrade(update.getGrade());
                    r.setStationCategory(update.getStationCategory());
                    r.setResultItemId(update.getResultItemId());
                    r.setIngredientItemId1(update.getIngredientItemId1());
                    r.setIngredientQuantity1(update.getIngredientQuantity1());
                    r.setIngredientItemId2(update.getIngredientItemId2());
                    r.setIngredientQuantity2(update.getIngredientQuantity2());
                    r.setIngredientItemId3(update.getIngredientItemId3());
                    r.setIngredientQuantity3(update.getIngredientQuantity3());
                    r.setIngredientItemId4(update.getIngredientItemId4());
                    r.setIngredientQuantity4(update.getIngredientQuantity4());
                    return recipeRepository.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }
}
