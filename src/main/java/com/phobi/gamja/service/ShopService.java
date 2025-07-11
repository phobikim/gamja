package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.ShopItemDto;
import com.phobi.gamja.dto.user.UserSellableItemDto;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemShopRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ItemShopRepository itemShopRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserDtlRepository userDtlRepository;

    // 판매 중인 상점 아이템 조회
    public ResponseEntity<GamJaResponse> getSellList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long gold = userDtlRepository.findById(userId)
                .map(user -> user.getGold())
                .orElse(0L);

        List<ShopItemDto> list = itemShopRepository.findByOnSaleTrue().stream()
                .map(ShopItemDto::from)
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

        List<UserSellableItemDto> list = userInventoryRepository.findByUserId(userId).stream()
                .map(UserSellableItemDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("보유 아이템 조회 완료", Map.of(
                "gold", gold,
                "items", list
        )));
    }

}
