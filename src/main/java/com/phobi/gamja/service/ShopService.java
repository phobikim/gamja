package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.ShopItemDto;
import com.phobi.gamja.dto.user.UserSellableItemDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemShop;
import com.phobi.gamja.entity.user.UserDailyActionLog;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.item.ItemShopRepository;
import com.phobi.gamja.repository.user.UserDailyActionLogRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ItemShopRepository itemShopRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserDtlRepository userDtlRepository;
    private final ItemRepository itemRepository;
    private final UserDailyActionLogRepository userDailyActionLogRepository;

    private final UserLogService userLogService;

    // 판매 중인 상점 아이템 조회
    public ResponseEntity<GamJaResponse> getSellList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long gold = userDtlRepository.findById(userId)
                .map(user -> user.getGold())
                .orElse(0L);
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();

        List<UserDailyActionLog> logs = userDailyActionLogRepository.findByUserIdAndLogDate(userId, today);
        Map<Long, Integer> boughtMap = logs.stream()
                .filter(log -> log.getItemId() != null)
                .collect(Collectors.toMap(
                        UserDailyActionLog::getItemId,
                        UserDailyActionLog::getCount
                ));

        List<ShopItemDto> list = itemShopRepository.findByOnSaleTrue().stream()
                .map(shopItem -> {
                    int bought = boughtMap.getOrDefault(shopItem.getItem().getId(), 0);
                    return ShopItemDto.from(shopItem, bought);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("상점 판매 아이템 조회 완료", Map.of(
                "gold", gold,
                "items", list
        )));
    }

    // 유저가 보유한 아이템 + 판매가격 조회
    public ResponseEntity<GamJaResponse> getUserInventoryForSale(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long gold = userDtlRepository.findById(userId)
                .map(user -> user.getGold())
                .orElse(0L);

        List<UserSellableItemDto> list = itemRepository.findSellableItemsWithUserQuantity(userId).stream()
                .map(proj -> UserSellableItemDto.builder()
                        .itemId(proj.getItemId())
                        .name(proj.getName())
                        .description(proj.getDescription())
                        .iconPath(proj.getIconPath())
                        .rank(proj.getRank())
                        .quantity(proj.getQuantity())
                        .sellPrice(proj.getSellPrice())
                        .build())
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("보유 아이템 조회 완료", Map.of(
                "gold", gold,
                "items", list
        )));
    }

    @Transactional
    public ResponseEntity<GamJaResponse> buyItem(HttpServletRequest request, Map<String, Long> payload) {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
        Long userId = (Long) request.getAttribute("userId");
        Long itemId = ((Number) payload.get("itemId")).longValue();
        int quantity = ((Number) payload.get("quantity")).intValue();

        ItemShop shopItem = itemShopRepository.findByItemIdAndOnSaleTrue(itemId)
                .orElseThrow(() -> new IllegalArgumentException("판매 중인 아이템이 아닙니다."));

        Integer userBoughtToday = userDailyActionLogRepository
                .findByUserIdAndLogDateAndMonsterIdAndItemId(userId, today, null, itemId)
                .map(UserDailyActionLog::getCount)
                .orElse(0);

        int maxPerDay = shopItem.getStock();
        int remaining = maxPerDay - userBoughtToday;
        if (quantity > remaining) {
            return ResponseEntity.ok(GamJaResponse.fail("구매 가능 수량을 초과했습니다."));
        }


        Item item = shopItem.getItem();
        int totalPrice = shopItem.getPrice() * quantity;

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        if (userDtl.getGold() < totalPrice) {
            return ResponseEntity.ok(GamJaResponse.fail("보유 골드가 부족합니다."));
        }

        // 골드 차감
        userDtl.setGold(userDtl.getGold() - totalPrice);
        userDtlRepository.save(userDtl);

        // 인벤토리 추가 or 갱신
        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseGet(() -> new UserInventory(userId, itemId, 0));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        userInventoryRepository.save(inventory);


        // 아이템 구매 이력 추가
        userLogService.recordDailyItemBuy(userId, itemId, quantity);

        return ResponseEntity.ok(GamJaResponse.success("아이템 구매 완료", Map.of(
                "itemId", itemId,
                "newGold", userDtl.getGold(),
                "newQuantity", inventory.getQuantity()
        )));
    }


    public ResponseEntity<GamJaResponse> sellItem(HttpServletRequest request, Map<String, Long> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long itemId = payload.get("itemId");
        int quantity = Integer.parseInt(payload.get("quantity").toString());


        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이템입니다."));

        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("보유하지 않은 아이템입니다."));

        if (inventory.getQuantity() < quantity) {
            return ResponseEntity.ok(GamJaResponse.fail("판매 수량이 보유 수량을 초과했습니다."));
        }

        int totalGain = item.getPrice() * quantity;

        // 수량 차감
        inventory.setQuantity(inventory.getQuantity() - quantity);

        // 0개면 삭제해도 됨 (선택)
        if (inventory.getQuantity() <= 0) {
            userInventoryRepository.delete(inventory);
        } else {
            userInventoryRepository.save(inventory);
        }

        // 골드 증가
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        userDtl.setGold(userDtl.getGold() + totalGain);
        userDtlRepository.save(userDtl);

        return ResponseEntity.ok(GamJaResponse.success("아이템 판매 완료", Map.of(
                "itemId", itemId,
                "newGold", userDtl.getGold(),
                "remainingQuantity", Math.max(0, inventory.getQuantity())
        )));
    }

}
