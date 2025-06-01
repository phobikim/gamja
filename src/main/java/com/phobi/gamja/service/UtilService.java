package com.phobi.gamja.service;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.dto.user.*;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UtilService {
    private final UserInventoryRepository userInventoryRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemRepository itemRepository;
    public List<UserInventoryDto> getUserInventoryWithEquipStatus(Long userId) {
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);
        List<UserEquipment> battleEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.EQUIP_BATTLE);
        List<UserEquipment> gatherEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.EQUIP_GATHER);

        Set<Long> equippedItemIds = Stream.concat(
                battleEquipments.stream(),
                gatherEquipments.stream()
        ).map(UserEquipment::getItemId).collect(Collectors.toSet());

        return inventoryList.stream().map(inv -> {
            Item item = inv.getItem();
            UserInventoryDto dto = new UserInventoryDto();

            dto.setItemId(inv.getItemId());
            dto.setQuantity(inv.getQuantity());
            dto.setUpdatedAt(inv.getUpdatedAt() != null ? inv.getUpdatedAt().toString() : null);
            dto.setEquipped(equippedItemIds.contains(inv.getItemId())); // 장착 여부

            if (item != null) {
                dto.setName(item.getName());
                dto.setDescription(item.getDescription());
                dto.setRank(item.getRank());
                dto.setRarity(item.getRarity().name());
                dto.setItemType(item.getItemType().name());
                dto.setEquipSlot(item.getEquipSlot().name());
                dto.setIconPath(item.getIconPath());
            }

            return dto;
        }).toList();
    }

    @Transactional
    public List<UserInventoryDto> equipItem(Long userId, Long itemId) {
        // 1. 아이템 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("아이템이 존재하지 않습니다."));

        // 2. 장착 가능한 장비인지 확인
        if (!item.getItemType().name().startsWith("EQUIP")) {
            throw new RuntimeException("장착할 수 없는 아이템입니다.");
        }

        // 3. 타입, 슬롯 변환
        EquipmentType type = EquipmentType.valueOf(item.getItemType().name());
        EquipmentSlot slot = EquipmentSlot.valueOf(item.getEquipSlot().name());

        // 4. 기존 장착 제거 → 동일 (userId + slot + type)
        userEquipmentRepository.deleteByUserIdAndSlotAndType(userId, slot, type);

        // 5. 새 장비 저장
        UserEquipment equip = new UserEquipment();
        equip.setUserId(userId);
        equip.setSlot(slot);
        equip.setType(type);
        equip.setItemId(itemId);
        equip.setEquippedAt(LocalDateTime.now());

        userEquipmentRepository.save(equip);

        // 6. 변경된 인벤토리 전체 반환
        return getUserInventoryWithEquipStatus(userId);
    }

}
