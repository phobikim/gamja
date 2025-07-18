package com.phobi.gamja.service;


import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemStatBonus;
import com.phobi.gamja.entity.item.ItemEnhanceMaterial;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.entity.user.UserEnhancement;
import com.phobi.gamja.entity.user.UserEnhancementId;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemEnhanceMaterialRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserEnhancementRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemEnhanceService {
    private final UserInventoryRepository userInventoryRepository;
    private final UserEnhancementRepository userEnhancementRepository;
    private final ItemEnhanceMaterialRepository itemEnhanceMaterialRepository;
    private final ItemRepository itemRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final UserDtlRepository userDtlRepository;

    private final CommonUtil commonUtil;
    private final LevelService levelService;
    public ResponseEntity<GamJaResponse> getEnhanceItemList(HttpSession session, Map<String, Long> payload) {
        Long userId = commonUtil.getUserId(session);
        Long itemId = ((Number) payload.get("itemId")).longValue();

        userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 인벤토리에 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 정보를 찾을 수 없습니다."));

        UserEnhancement enhancement = userEnhancementRepository.findById(new UserEnhancementId(userId, itemId))
                .orElse(null);

        int nextLevel = (enhancement != null ? enhancement.getEnhancementLevel() : 0) + 1;

        // 재료
        ItemEnhanceMaterial material = getRequiredMaterial(item, nextLevel);
        List<Map<String, Object>> materials = new ArrayList<>();
        addMaterialInfo(materials, material.getMaterialItemId1(), material.getMaterialQuantity1(), userId);
        addMaterialInfo(materials, material.getMaterialItemId2(), material.getMaterialQuantity2(), userId);
        addMaterialInfo(materials, material.getMaterialItemId3(), material.getMaterialQuantity3(), userId);

        // 유저 보유량
        Long goldOwned = userDtlRepository.findById(userId)
                .map(u -> u.getGold())
                .orElse(0L);


        Map<String, Object> result = Map.of(
                "gold", material.getGoldCost(),
                "goldOwned", goldOwned,
                "successRate", material.getSuccessRate(),
                "materials", materials
        );

        return ResponseEntity.ok(GamJaResponse.success("조회 완료", result));
    }
    public ResponseEntity<GamJaResponse> executeEnhance(HttpSession session, Map<String, Long> payload) {
        Long userId = commonUtil.getUserId(session);
        Long itemId = ((Number) payload.get("itemId")).longValue();

        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템을 보유하고 있지 않습니다."));

        UserEnhancement enhancement = userEnhancementRepository.findById(new UserEnhancementId(userId, itemId))
                .orElseGet(() -> {
                    ItemStatBonus base = itemStatBonusRepository.findById(itemId)
                            .orElseThrow(() -> new IllegalArgumentException("아이템 기본 스탯 없음"));

                    UserEnhancement newEnh = UserEnhancement.builder()
                            .userId(userId)
                            .itemId(itemId)
                            .enhancementLevel(0)
                            .enhancementXp(0)
                            .bonusPower(base.getBonusPower())
                            .bonusHp(base.getBonusHp())
                            .bonusSpeed(base.getBonusSpeed())
                            .build();
                    return userEnhancementRepository.save(newEnh);
                });
        int nextLevel = enhancement.getEnhancementLevel() + 1;

        // 3. 강화 재료 요구 조회
        Item item = itemRepository.findById(itemId).orElseThrow();
        ItemEnhanceMaterial requirement = itemEnhanceMaterialRepository
                .findWithAllSlotIncluded(item.getRarity(), item.getEquipSlot(), nextLevel)
                .orElseThrow(() -> new IllegalStateException("요구 강화 재료 정보가 없습니다."));
        // 4. 재료 확인 및 차감
        checkAndConsumeMaterial(userId, requirement.getMaterialItemId1(), requirement.getMaterialQuantity1());
        checkAndConsumeMaterial(userId, requirement.getMaterialItemId2(), requirement.getMaterialQuantity2());
        checkAndConsumeMaterial(userId, requirement.getMaterialItemId3(), requirement.getMaterialQuantity3());

        // 골드 확인 및 차감
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("유저 정보 없음"));
        if (userDtl.getGold() < requirement.getGoldCost()) {
            throw new IllegalArgumentException("골드가 부족합니다.");
        }

        userDtl.setGold(userDtl.getGold() - requirement.getGoldCost());
        // 5. 성공 여부 결정
        boolean success = Math.random() * 100 < requirement.getSuccessRate();
        if (success) {
            enhancement.setEnhancementLevel(nextLevel);
            enhancement.setBonusPower(enhancement.getBonusPower() + requirement.getBonusPower());
            enhancement.setBonusHp(enhancement.getBonusHp() + requirement.getBonusHp());
            enhancement.setBonusSpeed(enhancement.getBonusSpeed() + requirement.getBonusSpeed());

            enhancement.setEnhancementXp(0);
        } else {
            enhancement.setEnhancementXp(enhancement.getEnhancementXp() + 10);
        }

        enhancement.setEnhancementAt(LocalDateTime.now());
        userEnhancementRepository.save(enhancement);

        Map<String, Object> result = Map.of(
                "level", enhancement.getEnhancementLevel(),
                "xp", enhancement.getEnhancementXp(),
                "bonusPower", enhancement.getBonusPower(),
                "bonusHp", enhancement.getBonusHp(),
                "bonusSpeed", enhancement.getBonusSpeed(),
                "success", success
        );
        return ResponseEntity.ok(GamJaResponse.success("강화 완료", result));
    }

    private void checkAndConsumeMaterial(Long userId, Long itemId, int requiredQty) {
        if (itemId == null || requiredQty <= 0) return;

        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("[" + itemId + "] 재료 아이템이 부족합니다."));

        if (inventory.getQuantity() < requiredQty) {
            throw new IllegalArgumentException("[" + itemId + "] 재료 수량이 부족합니다.");
        }

        inventory.setQuantity(inventory.getQuantity() - requiredQty);
    }

    public ItemEnhanceMaterial getRequiredMaterial(Item item, int nextEnhanceLevel) {
        return itemEnhanceMaterialRepository
                .findWithAllSlotIncluded(item.getRarity(), item.getEquipSlot(), nextEnhanceLevel)
                .orElseThrow(() -> new IllegalStateException("요구 강화 재료 정보가 없습니다."));
    }

    private void addMaterialInfo(List<Map<String, Object>> list, Long itemId, int quantity, Long userId) {
        if (itemId == null || quantity <= 0) return;

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return;

        int owned = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .map(inv -> inv.getQuantity())
                .orElse(0);

        list.add(Map.of(
                "itemId", itemId,
                "name", item.getName(),
                "iconPath", item.getIconPath(),
                "quantity", quantity,
                "owned", owned
        ));
    }
}
