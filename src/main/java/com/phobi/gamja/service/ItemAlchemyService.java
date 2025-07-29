package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemAlchemyCost;
import com.phobi.gamja.entity.item.ItemAlchemyOption;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemAlchemyCostRepository;
import com.phobi.gamja.repository.item.ItemAlchemyOptionRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.repository.user.UserItemAlchemyOptionRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemAlchemyService {
    private final CommonUtil commonUtil;
    private final UserInventoryRepository userInventoryRepository;
    private final ItemRepository itemRepository;
    private final UserDtlRepository userDtlRepository;
    private final ItemAlchemyOptionRepository itemAlchemyOptionRepository;
    private final UserItemAlchemyOptionRepository userItemAlchemyOptionRepository;
    private final ItemAlchemyCostRepository itemAlchemyCostRepository;

    public ResponseEntity<GamJaResponse> getAvailableAlchemyOptions(HttpSession session, Map<String, Long> payload) {
        Long userId = commonUtil.getUserId(session);
        Long itemId = payload.get("itemId");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 정보를 찾을 수 없습니다."));

        List<ItemAlchemyOption> optionList = itemAlchemyOptionRepository
                .findByRarityAndEquipSlot(item.getRarity(), item.getEquipSlot());

        List<Map<String, Object>> result = optionList.stream()
                .map(opt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("optionType", opt.getOptionType().name());
                    map.put("valueType", opt.getValueType().name());
                    map.put("min", opt.getMinValue());
                    map.put("max", opt.getMaxValue());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("연금 옵션 조회 완료", result));
    }

    public ResponseEntity<GamJaResponse> getAlchemyMaterialInfo(HttpSession session, Map<String, Long> payload) {
        Long userId = commonUtil.getUserId(session);
        Long itemId = payload.get("itemId");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 정보를 찾을 수 없습니다."));

        userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 인벤토리에 없습니다."));

        // 골드 보유량
        Long goldOwned = userDtlRepository.findById(userId)
                .map(UserDtl::getGold)
                .orElse(0L);

        // 비용 정보 조회
        ItemAlchemyCost cost = itemAlchemyCostRepository
                .findByRarityAndEquipSlot(item.getRarity(), item.getEquipSlot())
                .orElseThrow(() -> new IllegalArgumentException("연금 비용 정보가 없습니다."));

        List<Map<String, Object>> materials = new ArrayList<>();
        addMaterialInfo(materials, cost.getMaterialItemId1(), cost.getMaterialQuantity1(), userId);
        addMaterialInfo(materials, cost.getMaterialItemId2(), cost.getMaterialQuantity2(), userId);
        addMaterialInfo(materials, cost.getMaterialItemId3(), cost.getMaterialQuantity3(), userId);

        int optionCount = getOptionCountByRarity(item.getRarity());
        Map<String, Object> result = Map.of(
                "gold", cost.getGoldCost(),
                "goldOwned", goldOwned,
                "materials", materials,
                "optionCount", optionCount
        );

        return ResponseEntity.ok(GamJaResponse.success("연금 정보 조회 완료", result));
    }

    @Transactional
    public ResponseEntity<GamJaResponse> executeAlchemy(HttpSession session, Map<String, Long> payload) {
        Long userId = commonUtil.getUserId(session);
        Long itemId = payload.get("itemId");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템이 존재하지 않습니다."));

        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템을 보유하고 있지 않습니다."));

        // 1. 후보 옵션들 가져오기
        List<ItemAlchemyOption> options = itemAlchemyOptionRepository
                .findByRarityAndEquipSlot(item.getRarity(), item.getEquipSlot());

        if (options.isEmpty()) {
            throw new IllegalArgumentException("연금 가능한 옵션이 없습니다.");
        }

        // 3. 재료 및 골드 차감
        // 비용 정보 조회
        ItemAlchemyCost cost = itemAlchemyCostRepository
                .findByRarityAndEquipSlot(item.getRarity(), item.getEquipSlot())
                .orElseThrow(() -> new IllegalArgumentException("연금 비용 정보가 없습니다."));

        // 재료 차감
        consumeMaterial(userId, cost.getMaterialItemId1(), cost.getMaterialQuantity1());
        consumeMaterial(userId, cost.getMaterialItemId2(), cost.getMaterialQuantity2());
        consumeMaterial(userId, cost.getMaterialItemId3(), cost.getMaterialQuantity3());

        // 골드 차감
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음"));
        if (userDtl.getGold() < cost.getGoldCost()) {
            throw new IllegalArgumentException("골드가 부족합니다.");
        }
        userDtl.setGold(userDtl.getGold() - cost.getGoldCost());

        // 기존 옵션 삭제
        userItemAlchemyOptionRepository.deleteByUserIdAndItemId(userId, itemId);

        // 등급에 따른 옵션 개수만큼 새로 뽑기
        int optionCount = getOptionCountByRarity(item.getRarity());
        List<UserItemAlchemyOption> resultOptions = new ArrayList<>();
        for (int i = 1; i <= optionCount; i++) {
            ItemAlchemyOption selected = pickRandomOption(options);
            BigDecimal value = selected.getOptionType() == ItemAlchemyOption.OptionType.JUNK
                    ? null
                    : randomInRange(selected.getMinValue(), selected.getMaxValue());

            resultOptions.add(UserItemAlchemyOption.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .optionIndex(i)
                    .optionType(selected.getOptionType())
                    .valueType(selected.getValueType())
                    .optionValue(value)
                    .description(selected.getDescription())
                    .build());
        }

        userItemAlchemyOptionRepository.saveAll(resultOptions);

        List<Map<String, Object>> result = resultOptions.stream()
                .map(opt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("optionIndex", opt.getOptionIndex());
                    map.put("optionType", opt.getOptionType());
                    map.put("value", opt.getOptionValue());
                    map.put("valueType", opt.getValueType());
                    map.put("description", opt.getDescription());
                    return map;
                })
                .collect(Collectors.toList());


        return ResponseEntity.ok(GamJaResponse.success("연금 완료", result));
    }

    private ItemAlchemyOption pickRandomOption(List<ItemAlchemyOption> options) {
        int totalWeight = options.stream().mapToInt(opt -> opt.getWeight() != null ? opt.getWeight() : 1).sum();
        int rand = (int) (Math.random() * totalWeight);
        int cumulative = 0;
        for (ItemAlchemyOption option : options) {
            cumulative += option.getWeight() != null ? option.getWeight() : 1;
            if (rand < cumulative) return option;
        }
        return options.get(0); // fallback
    }

    private BigDecimal randomInRange(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) return null;
        int minInt = min.intValue();
        int maxInt = max.intValue();
        int randomInt = minInt + (int)(Math.random() * (maxInt - minInt + 1));
        return BigDecimal.valueOf(randomInt);
    }


    private void consumeMaterial(Long userId, Long itemId, Integer quantity) {
        if (itemId == null || quantity == null || quantity <= 0) return;
        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("[" + itemId + "] 재료가 없습니다."));
        if (inventory.getQuantity() < quantity) {
            throw new IllegalArgumentException("[" + itemId + "] 재료 수량 부족");
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
    }


    private void addMaterialInfo(List<Map<String, Object>> list, Long itemId, Integer qty, Long userId) {
        if (itemId == null || qty == null || qty <= 0) return;
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return;
        int owned = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .map(UserInventory::getQuantity)
                .orElse(0);
        list.add(Map.of(
                "itemId", itemId,
                "name", item.getName(),
                "quantity", qty,
                "owned", owned,
                "iconPath", item.getIconPath()
        ));
    }

    private int getOptionCountByRarity(Item.Rarity rarity) {
        switch (rarity) {
            case COMMON:
            case UNCOMMON:
                return 1;
            case RARE:
                return 2;
            case EPIC:
                return 3;
            default:
                return 1;
        }
    }


}