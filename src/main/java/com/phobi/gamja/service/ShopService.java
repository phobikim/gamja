package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.ShopItemDto;
import com.phobi.gamja.dto.user.UserSellableItemDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemShop;
import com.phobi.gamja.entity.skin.SkinBorder;
import com.phobi.gamja.entity.user.UserDailyActionLog;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.entity.user.UserSkin;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.item.ItemShopRepository;
import com.phobi.gamja.repository.skin.SkinBorderRepository;
import com.phobi.gamja.repository.user.UserDailyActionLogRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.repository.user.UserSkinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ItemShopRepository itemShopRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserSkinRepository userSkinRepository;
    private final ItemRepository itemRepository;
    private final SkinBorderRepository skinBorderRepository;
    private final UserDailyActionLogRepository userDailyActionLogRepository;

    private final UserLogService userLogService;

    // 판매 중인 상점 아이템 조회
    public ResponseEntity<GamJaResponse> getSellList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Long gold = userDtlRepository.findById(userId)
                .map(UserDtl::getGold)
                .orElse(0L);

        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
        // 일일 구매 로그 → targetId 기준으로 합산 (item/skin 공통 키)
        // (동일 targetId 다회 구매 안전 처리: groupingBy + sum)
        Map<Long, Integer> boughtMap = userDailyActionLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .filter(log -> log.getItemId() != null) // ← 로그에 targetId를 itemId 칼럼에 저장한다고 가정
                .collect(Collectors.groupingBy(
                        UserDailyActionLog::getItemId,
                        Collectors.summingInt(UserDailyActionLog::getCount)
                ));

        // 상점 목록(판매중)
        List<ItemShop> shops = itemShopRepository.findByOnSaleTrueOrderByDisplayOrderAsc();

        // 카테고리별 대상 ID 배치 수집
        List<Long> adventureIds = shops.stream()
                .filter(s -> s.getCategory() == ItemShop.ItemCategory.ADVENTURE)
                .map(ItemShop::getTargetId)
                .distinct()
                .toList();

        List<Long> skinIds = shops.stream()
                .filter(s -> s.getCategory() == ItemShop.ItemCategory.SKIN)
                .map(ItemShop::getTargetId)
                .distinct()
                .toList();
        // 배치 조회 → 맵핑
        Map<Long, Item> itemMap = adventureIds.isEmpty()
                ? Map.of()
                : itemRepository.findAllById(adventureIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        Map<Long, SkinBorder> skinMap = skinIds.isEmpty()
                ? Map.of()
                : skinBorderRepository.findAllById(skinIds).stream()
                .collect(Collectors.toMap(SkinBorder::getId, Function.identity()));

        Set<Long> ownedSkinIds = skinIds.isEmpty()
                ? Set.of()
                : new HashSet<>(userSkinRepository.findOwnedSkinBorderIdsByUserId(userId));
        // DTO 변환 (원래 display_order 순서 유지)
        List<ShopItemDto> list = new ArrayList<>(shops.size());
        for (ItemShop s : shops) {
            int bought = boughtMap.getOrDefault(s.getTargetId(), 0);

            switch (s.getCategory()) {
                case ADVENTURE -> {
                    Item item = itemMap.get(s.getTargetId());
                    if (item == null) continue; // 안전장치: 잘못 등록된 상점행 스킵
                    list.add(ShopItemDto.fromAdventure(s, item, bought));
                }
                case SKIN -> {
                    SkinBorder skin = skinMap.get(s.getTargetId());
                    if (skin == null) continue;
                    boolean owned = ownedSkinIds.contains(s.getTargetId());
                    list.add(ShopItemDto.fromSkin(s, skin, bought, owned));
                }
            }
        }

        return ResponseEntity.ok(GamJaResponse.success(
                "상점 판매 아이템 조회 완료",
                Map.of("gold", gold, "items", list)
        ));
    }

    // 유저가 보유한 아이템 + 판매가격 조회
    public ResponseEntity<GamJaResponse> getUserInventoryForSale(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long gold = userDtlRepository.findById(userId)
                .map(user -> user.getGold())
                .orElse(0L);

        List<UserSellableItemDto> list = itemRepository.findSellableItemsWithUserQuantity(userId).stream()
                .map(proj -> UserSellableItemDto.builder()
                        .targetId(proj.getItemId())
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
    public ResponseEntity<GamJaResponse> buyItem(HttpServletRequest request, Map<String, Object> payload) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(GamJaResponse.fail("세션이 없습니다."));
        }

        Object rawTarget = payload.get("targetId");
        Long targetId = (rawTarget instanceof Number)
                ? ((Number) rawTarget).longValue()
                : Long.parseLong(String.valueOf(rawTarget));

        Object rawQty = payload.get("quantity");
        int quantity = (rawQty instanceof Number)
                ? ((Number) rawQty).intValue()
                : Integer.parseInt(String.valueOf(rawQty));

        String categoryStr = String.valueOf(payload.get("category"));
        ItemShop.ItemCategory category = ItemShop.ItemCategory.valueOf(categoryStr.toUpperCase());

        // 상점행 조회 (판매중 + category + targetId)
        ItemShop shop = (ItemShop) itemShopRepository.findByCategoryAndTargetIdAndOnSaleTrue(category, targetId)
                .orElseThrow(() -> new IllegalArgumentException("판매 중인 상품이 아닙니다."));
        // 오늘 기준
        LocalDate todayKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();

        // 일일 구매 수량
        int boughtToday = userDailyActionLogRepository
                .findByUserIdAndLogDateAndMonsterIdAndItemId(userId, todayKst, null, shop.getTargetId())
                .map(UserDailyActionLog::getCount)
                .orElse(0);

        int maxPerDay = (shop.getStock() != null) ? shop.getStock() : 99;
        int remaining = Math.max(0, maxPerDay - boughtToday);
        if (quantity > remaining) {
            return ResponseEntity.ok(GamJaResponse.fail("구매 가능 수량을 초과했습니다."));
        }
        int totalPrice = shop.getPrice() * quantity;

        UserDtl user = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
        if (user.getGold() < totalPrice) {
            return ResponseEntity.ok(GamJaResponse.fail("보유 골드가 부족합니다."));
        }
        user.setGold(user.getGold() - totalPrice);
        userDtlRepository.save(user);

        Integer newQuantity = null;
        switch (shop.getCategory()) {
            case ADVENTURE -> {
                // 아이템 존재 검증
                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalStateException("아이템이 존재하지 않습니다: " + targetId));

                // 인벤토리 추가/갱신
                UserInventory inv = userInventoryRepository
                        .findByUserIdAndItemId(userId, targetId)
                        .orElseGet(() -> new UserInventory(userId, targetId, 0));
                inv.setQuantity(inv.getQuantity() + quantity);
                userInventoryRepository.save(inv);
                newQuantity = inv.getQuantity();

                userLogService.recordDailyItemBuy(userId, targetId, quantity);
            }
            case SKIN -> {
                // 스킨 존재 검증
                SkinBorder skin = skinBorderRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalStateException("스킨이 존재하지 않습니다: " + targetId));
                if (quantity != 1) {
                    return ResponseEntity.ok(GamJaResponse.fail("스킨은 수량 1개만 구매할 수 있습니다."));
                }

                // 3) 보유 여부 확인 (중복 구매 불가)
                boolean owned = userSkinRepository.existsByUserIdAndSkinBorder_Id(userId, targetId);
                if (owned) {
                    return ResponseEntity.ok(GamJaResponse.fail("이미 보유한 스킨입니다."));
                }
                // 4) 보유 등록
                UserSkin userSkin = new UserSkin();
                userSkin.setUserId(userId);
                userSkin.setSkinBorder(skin);
                userSkin.setCreatedAt(LocalDateTime.now());
                userSkinRepository.save(userSkin);

                UserDtl userDtl = userDtlRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
                userDtl.setBorderSkin(skin);
                userDtlRepository.save(userDtl);
            }
            default -> {
                // no-op
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("category", shop.getCategory().name());
        data.put("targetId", targetId);
        data.put("newGold", user.getGold());
        if (newQuantity != null) data.put("newQuantity", newQuantity);
        return ResponseEntity.ok(GamJaResponse.success("구매 완료", data));
    }


    public ResponseEntity<GamJaResponse> sellItem(HttpServletRequest request, Map<String, Long> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long targetId = payload.get("targetId");
        int quantity = Integer.parseInt(payload.get("quantity").toString());


        Item item = itemRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이템입니다."));

        UserInventory inventory = userInventoryRepository.findByUserIdAndItemId(userId, targetId)
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
                "targetId", targetId,
                "newGold", userDtl.getGold(),
                "remainingQuantity", Math.max(0, inventory.getQuantity())
        )));
    }

}
