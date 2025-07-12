package com.phobi.gamja.service;

import com.phobi.gamja.dto.dex.DexGrowthRequest;
import com.phobi.gamja.dto.item.GrowthItemDto;
import com.phobi.gamja.dto.user.UserDexXpDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemExpBonus;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DexGrowthService {
    private final UserInventoryRepository userInventoryRepository;
    private final ItemRepository itemRepository;

    private final CommonUtil commonUtil;
    private final LevelService levelService;

    public ResponseEntity<GamJaResponse> getGrowthItemList(HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        List<Item> growthItems = itemRepository.findByItemType(Item.ItemType.GROWTH);
        List<UserInventory> inventories = userInventoryRepository.findByUserId(userId);
        Map<Long, Integer> itemIdToQuantity = inventories.stream()
                .collect(Collectors.toMap(
                        inv -> inv.getItem().getId(),
                        UserInventory::getQuantity
                ));

        List<GrowthItemDto> result = growthItems.stream()
                .map(item -> GrowthItemDto.builder()
                        .itemId(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .iconPath(item.getIconPath())
                        .quantity(itemIdToQuantity.getOrDefault(item.getId(), 0))
                        .bonusExp(item.getExpBonus() != null ? item.getExpBonus().getBonusExp() : 0)
                        .build())
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }

    @Transactional
    public ResponseEntity<GamJaResponse> executeGrowth(Long userId, DexGrowthRequest dto) {
        Long dexId = dto.getDexId();
        Long itemId = dto.getItemId();
        int quantity = dto.getQuantity();

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("사용 수량이 유효하지 않습니다."));
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));

        // ✅ 인벤토리 수량 확인 및 차감
        int owned = Optional.ofNullable(userInventoryRepository.getQuantity(userId, itemId)).orElse(0);
        if (owned < quantity) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("아이템 수량이 부족합니다."));
        }
        int updated = userInventoryRepository.consumeItem(userId, itemId, quantity);
        if (updated == 0) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("아이템 차감 실패"));
        }

        ItemExpBonus bonus = item.getExpBonus();
        int gainedXp = (bonus != null ? bonus.getBonusExp() : 0) * quantity;

        UserDexXpDto xpResult = levelService.updateCharacterExp(userId, dexId, gainedXp);

        return ResponseEntity.ok(GamJaResponse.success("성장 완료", xpResult));
    }
}
