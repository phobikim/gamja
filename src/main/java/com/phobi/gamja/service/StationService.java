package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.ItemRecipeDto;
import com.phobi.gamja.dto.contents.StationDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemRecipe;
import com.phobi.gamja.entity.item.ItemSkillBonus;
import com.phobi.gamja.entity.item.ItemStatBonus;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.CraftRequest;
import com.phobi.gamja.repository.contents.StationRepository;
import com.phobi.gamja.repository.item.ItemRecipeRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final ItemRecipeRepository itemRecipeRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;

    public List<StationDto> getStationList() {
        return stationRepository.findAll().stream()
                .map(s -> StationDto.builder()
                        .name(s.getName())
                        .category(s.getCategory())
                        .imagePath(s.getImagePath())
                        .build())
                .toList();
    }

    public List<ItemRecipeDto> getRecipeList(Long userId, String stationCategory) {
        ItemRecipe.StationCategory categoryEnum = ItemRecipe.StationCategory.valueOf(stationCategory);
        List<ItemRecipe> recipes = itemRecipeRepository.findByStationCategory(categoryEnum);
        Map<Long, Item> itemMap = getAllRelatedItems(recipes);
        Map<Long, Integer> inventoryMap = getInventoryMap(userId);
        return toRecipeDtos(recipes, itemMap, inventoryMap);
    }


    public List<ItemRecipeDto> craftItem(Long userId, String stationCategory, CraftRequest request) {
        // 1. 재료 소모
        for (CraftRequest.CraftIngredient ing : request.getIngredients()) {
            UserInventory inv = userInventoryRepository
                    .findByUserIdAndItemId(userId, ing.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("재료 부족: " + ing.getItemId()));

            if (inv.getQuantity() < ing.getQuantity()) {
                throw new IllegalArgumentException("재료 부족: " + ing.getItemId());
            }
            inv.setQuantity(inv.getQuantity() - ing.getQuantity());
            userInventoryRepository.save(inv);
        }

        // 2. 결과 아이템 지급
        UserInventory result = userInventoryRepository
                .findByUserIdAndItemId(userId, request.getResultItemId())
                .orElseGet(() -> {
                    UserInventory newInv = new UserInventory();
                    newInv.setUserId(userId);
                    newInv.setItemId(request.getResultItemId());
                    newInv.setQuantity(0);
                    return newInv;
                });

        result.setQuantity(result.getQuantity() + request.getResultQuantity());
        userInventoryRepository.save(result);

        return getRecipeList(userId, stationCategory);
    }

    private List<ItemRecipeDto> toRecipeDtos(List<ItemRecipe> recipes,
                                             Map<Long, Item> itemMap,
                                             Map<Long, Integer> inventoryMap) {
        List<ItemRecipeDto> dtoList = recipes.stream()
                .map(recipe -> {
                    Item resultItem = itemMap.get(recipe.getResultItemId());
                    if (resultItem == null) return null;

                    List<ItemRecipeDto.IngredientDto> ingredients = new ArrayList<>();
                    addIng(recipe.getIngredientItemId1(), recipe.getIngredientQuantity1(), itemMap, inventoryMap, ingredients);
                    addIng(recipe.getIngredientItemId2(), recipe.getIngredientQuantity2(), itemMap, inventoryMap, ingredients);
                    addIng(recipe.getIngredientItemId3(), recipe.getIngredientQuantity3(), itemMap, inventoryMap, ingredients);
                    addIng(recipe.getIngredientItemId4(), recipe.getIngredientQuantity4(), itemMap, inventoryMap, ingredients);

                    ItemRecipeDto.ItemRecipeDtoBuilder builder = ItemRecipeDto.builder()
                            .recipeId(recipe.getId())
                            .recipeName(recipe.getName())
                            .recipeDescription(resultItem.getDescription())
                            .grade(recipe.getGrade().name())
                            .resultItemId(resultItem.getId())
                            .resultItemName(resultItem.getName())
                            .resultItemIcon(resultItem.getIconPath())
                            .resultItemUserOwned(inventoryMap.getOrDefault(resultItem.getId(), 0))
                            .ingredients(ingredients);

                    if (resultItem.getItemType() == Item.ItemType.EQUIP_BATTLE) {
                        ItemStatBonus stat = itemStatBonusRepository.findById(resultItem.getId()).orElse(null);
                        builder.baseHp(stat != null ? stat.getBonusHp() : 0);
                        builder.basePower(stat != null ? stat.getBonusPower() : 0);
                        builder.baseSpeed(stat != null ? stat.getBonusSpeed() : 0);
                    } else if (resultItem.getItemType() == Item.ItemType.EQUIP_GATHER) {
                        ItemSkillBonus bonus = itemSkillBonusRepository.findById(resultItem.getId()).orElse(null);
                        builder.fishing(bonus != null ? bonus.getFishing() : 0);
                        builder.mining(bonus != null ? bonus.getMining() : 0);
                        builder.woodcutting(bonus != null ? bonus.getWoodcutting() : 0);
                        builder.gathering(bonus != null ? bonus.getGathering() : 0);
                        builder.making(bonus != null ? bonus.getMaking() : 0);
                    }

                    return builder.build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        dtoList.sort(Comparator.comparing(dto -> UtilService.RARITY_ORDER.getOrDefault(dto.getGrade(), 99)));
        return dtoList;

    }

    private void addIng(Long itemId, Integer qty,
                        Map<Long, Item> itemMap,
                        Map<Long, Integer> inventoryMap,
                        List<ItemRecipeDto.IngredientDto> list) {
        if (itemId == null || qty == null) return;
        Item item = itemMap.get(itemId);
        if (item == null) return;

        list.add(ItemRecipeDto.IngredientDto.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .itemIcon(item.getIconPath())
                .quantity(qty)
                .userOwned(inventoryMap.getOrDefault(item.getId(), 0))
                .build());
    }

    private Map<Long, Item> getAllRelatedItems(List<ItemRecipe> recipes) {
        Set<Long> itemIds = new HashSet<>();
        for (ItemRecipe r : recipes) {
            itemIds.add(r.getResultItemId());
            Collections.addAll(itemIds,
                    r.getIngredientItemId1(), r.getIngredientItemId2(),
                    r.getIngredientItemId3(), r.getIngredientItemId4());
        }
        itemIds.remove(null);
        return itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
    }

    private Map<Long, Integer> getInventoryMap(Long userId) {
        return userInventoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInventory::getItemId, UserInventory::getQuantity));
    }
}
