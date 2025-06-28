package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.ItemRecipeDto;
import com.phobi.gamja.dto.contents.StationDto;
import com.phobi.gamja.entity.item.*;
import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.CraftRequest;
import com.phobi.gamja.repository.contents.StationRepository;
import com.phobi.gamja.repository.item.*;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.phobi.gamja.service.UtilService.RARITY_ORDER;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final ItemRecipeRepository itemRecipeRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;
    private final ItemPotionEffectRepository itemPotionEffectRepository;
    private final LogService logService;

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

    @Transactional
    public void craftItem(HttpSession session, String stationCategory, CraftRequest request) {
        Long userId = (Long) session.getAttribute("userId");
        Long resultItemId = request.getResultItemId();
        int resultQuantity = request.getResultQuantity();
        // 1. 레시피 조회
        ItemRecipe recipe = itemRecipeRepository.findByResultItemId(resultItemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제작 레시피입니다."));

        // 재료 차감
        for (int i = 1; i <= 4; i++) {
            Long itemId = getIngredientItemId(recipe, i);
            Integer quantity = getIngredientQuantity(recipe, i);
            if (itemId == null || quantity == null || quantity <= 0) continue;

            int totalRequired = quantity * resultQuantity;
            UserInventory inv = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                    .orElseThrow(() -> new IllegalArgumentException("재료가 부족합니다."));
            if (inv.getQuantity() < totalRequired) {
                throw new IllegalArgumentException("재료가 부족합니다.");
            }
            inv.setQuantity(inv.getQuantity() - totalRequired);
            userInventoryRepository.save(inv);
        }
        // 결과 아이템 지급
        UserInventory result = userInventoryRepository
                .findByUserIdAndItemId(userId, resultItemId)
                .orElseGet(() -> new UserInventory(userId, resultItemId, 0));
        result.setQuantity(result.getQuantity() + resultQuantity);
        userInventoryRepository.save(result);

        logService.recordCounter(userId, CounterType.ITEM_CRAFT, resultItemId, resultQuantity);
    }

    private Long getIngredientItemId(ItemRecipe recipe, int index) {
        return switch (index) {
            case 1 -> recipe.getIngredientItemId1();
            case 2 -> recipe.getIngredientItemId2();
            case 3 -> recipe.getIngredientItemId3();
            case 4 -> recipe.getIngredientItemId4();
            default -> null;
        };
    }

    private Integer getIngredientQuantity(ItemRecipe recipe, int index) {
        return switch (index) {
            case 1 -> recipe.getIngredientQuantity1();
            case 2 -> recipe.getIngredientQuantity2();
            case 3 -> recipe.getIngredientQuantity3();
            case 4 -> recipe.getIngredientQuantity4();
            default -> null;
        };
    }


    private ItemRecipeDto toRecipeDto(ItemRecipe recipe,
                                      Map<Long, Item> itemMap,
                                      Map<Long, Integer> inventoryMap) {
        return toRecipeDtos(List.of(recipe), itemMap, inventoryMap).get(0);
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
                    } else if (resultItem.getItemType() == Item.ItemType.EQUIP_POTION) {
                        ItemPotionEffect effect = itemPotionEffectRepository.findById(resultItem.getId()).orElse(null);
                        builder.baseHp(effect != null ? effect.getHealHp() : 0);
                        builder.basePower(effect != null ? effect.getBonusPower() : 0);
                    }

                    return builder.build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        dtoList.sort(Comparator
                .comparing((ItemRecipeDto dto) -> RARITY_ORDER.getOrDefault(dto.getGrade(), 99))
                .thenComparing(dto -> dto.getBasePower() != null ? dto.getBasePower() : Integer.MAX_VALUE)
        );
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
