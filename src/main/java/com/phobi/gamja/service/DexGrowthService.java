package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.GrowthItemDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.UserInventory;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.user.UserInventoryRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DexGrowthService {
    private final UserInventoryRepository userInventoryRepository;
    private final CommonUtil commonUtil;
    public ResponseEntity<GamJaResponse> getGrowthItemList(HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        List<UserInventory> inventories = userInventoryRepository.findByUserId(userId);
        List<GrowthItemDto> result = inventories.stream()
                .filter(inv -> inv.getQuantity() > 0) // 수량 필터
                .filter(inv -> inv.getItem().getItemType() == Item.ItemType.GROWTH) // GROWTH 타입 필터
                .map(inv -> {
                    var item = inv.getItem();
                    var expBonus = item.getExpBonus(); // 연관된 경험치 효과
                    return GrowthItemDto.builder()
                            .itemId(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .iconPath(item.getIconPath())
                            .quantity(inv.getQuantity())
                            .bonusExp(expBonus != null ? expBonus.getBonusExp() : 0)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }
}
