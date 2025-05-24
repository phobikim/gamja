package com.example.gamja.controller;

import com.example.gamja.dto.RecipeDto;
import com.example.gamja.dto.StationDto;
import com.example.gamja.dto.UserInventoryDto;
import com.example.gamja.entity.Item;
import com.example.gamja.entity.ItemRecipe;
import com.example.gamja.entity.Station;
import com.example.gamja.entity.UserInventory;
import com.example.gamja.message.CraftRequest;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import com.example.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/station")
public class StationController {
    private final CommonUtil commonUtil;
    private final UserDtlRepository userDtlRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final StationRepository stationRepository;
    private final ItemRecipeRepository itemRecipeRepository;
    private final ItemRepository itemRepository;


    @ResponseBody
    @GetMapping("/list/{userId}")
    public ResponseEntity<GamJaResponse> getStationList (@PathVariable Long userId, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        List<Station> stationList = stationRepository.findAll();

        List<StationDto> responseList = stationList.stream().map(s -> {
            StationDto dto = new StationDto();

            dto.setName(s.getName());
            dto.setCategory(s.getCategory());
            dto.setImagePath(s.getImagePath());

            return dto;
        }).toList();
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", responseList));
    }

    @ResponseBody
    @GetMapping("/recipe/{stationCategory}/{userId}")
    public ResponseEntity<GamJaResponse> getRecipeWithInventory (
            @PathVariable String stationCategory,
            @PathVariable Long userId,
            HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        ItemRecipe.StationCategory categoryEnum = ItemRecipe.StationCategory.valueOf(stationCategory);
        List<ItemRecipe> recipes = itemRecipeRepository.findByStationCategory(categoryEnum);
        Map<Long, Item> itemMap = getAllRelatedItems(recipes);

        Map<Long, Integer> inventoryMap = userInventoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInventory::getItemId, UserInventory::getQuantity));

        List<RecipeDto> result = recipes.stream()
                .map(recipe -> toRecipeDto(recipe, itemMap, inventoryMap))
                .filter(Objects::nonNull)
                .toList();
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }


    @ResponseBody
    @PostMapping("/craft/{userId}")
    public ResponseEntity<GamJaResponse> itemCraft (
            @PathVariable Long userId,
            @RequestBody CraftRequest request,
            HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        // 1. 재료 소모
        for (CraftRequest.CraftIngredient ing : request.getIngredients()) {
            UserInventory inv = userInventoryRepository
                    .findByUserIdAndItemId(userId, ing.getItemId())
                    .orElse(null);
            if (inv == null || inv.getQuantity() < ing.getQuantity()) {
                return ResponseEntity.badRequest().body(GamJaResponse.fail("재료 부족: " + ing.getItemId()));
            }
            inv.setQuantity(inv.getQuantity() - ing.getQuantity());
            userInventoryRepository.save(inv);
        }

        // 2. 결과 아이템 추가
        UserInventory result = userInventoryRepository.
                findByUserIdAndItemId(userId, request.getResultItemId())
                .orElse(null);
        if (result == null) {
            result = new UserInventory();
            result.setUserId(userId);
            result.setItemId(request.getResultItemId());
            result.setQuantity(request.getResultQuantity());
        } else {
            result.setQuantity(result.getQuantity() + request.getResultQuantity());
        }
        userInventoryRepository.save(result);

        return ResponseEntity.ok(GamJaResponse.success("제작 완료", null));

    }

    private RecipeDto toRecipeDto(ItemRecipe recipe, Map<Long, Item> itemMap, Map<Long, Integer> inventoryMap) {
        Item resultItem = itemMap.get(recipe.getResultItemId());
        if (resultItem == null) return null;

        // user 가 가진 아이템 수
        int resultItemUserOwned = inventoryMap.getOrDefault(resultItem.getId(), 0);

        List<RecipeDto.IngredientDto> ingredients = new ArrayList<>();
        addIngredient(recipe.getIngredientItemId1(), recipe.getIngredientQuantity1(), itemMap, inventoryMap, ingredients);
        addIngredient(recipe.getIngredientItemId2(), recipe.getIngredientQuantity2(), itemMap, inventoryMap, ingredients);
        addIngredient(recipe.getIngredientItemId3(), recipe.getIngredientQuantity3(), itemMap, inventoryMap, ingredients);
        addIngredient(recipe.getIngredientItemId4(), recipe.getIngredientQuantity4(), itemMap, inventoryMap, ingredients);

        return RecipeDto.builder()
                .recipeId(recipe.getId())
                .recipeName(recipe.getName())
                .recipeDescription(resultItem.getDescription())
                .grade(recipe.getGrade().toString())
                .resultItemId(resultItem.getId())
                .resultItemName(resultItem.getName())
                .resultItemIcon(resultItem.getIconPath())
                .resultItemUserOwned(resultItemUserOwned)
                .ingredients(ingredients)
                .build();
    }
    private void addIngredient(Long itemId, Integer qty,
                               Map<Long, Item> itemMap,
                               Map<Long, Integer> inventoryMap,
                               List<RecipeDto.IngredientDto> list) {
        if (itemId == null || qty == null) return;

        Item item = itemMap.get(itemId);
        if (item == null) return;

        int userHas = inventoryMap.getOrDefault(itemId, 0);

        list.add(RecipeDto.IngredientDto.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .itemIcon(item.getIconPath())
                .quantity(qty)
                .userOwned(userHas)
                .build());
    }

    private Map<Long, Item> getAllRelatedItems(List<ItemRecipe> recipes) {
        Set<Long> itemIds = new HashSet<>();

        for (ItemRecipe recipe : recipes) {
            itemIds.add(recipe.getResultItemId());
            if (recipe.getIngredientItemId1() != null) itemIds.add(recipe.getIngredientItemId1());
            if (recipe.getIngredientItemId2() != null) itemIds.add(recipe.getIngredientItemId2());
            if (recipe.getIngredientItemId3() != null) itemIds.add(recipe.getIngredientItemId3());
            if (recipe.getIngredientItemId4() != null) itemIds.add(recipe.getIngredientItemId4());
        }

        return itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
    }

}
