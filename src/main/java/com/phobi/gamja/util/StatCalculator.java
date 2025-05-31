package com.phobi.gamja.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.phobi.gamja.dto.item.ItemDto;
import com.phobi.gamja.dto.user.*;
import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemStatBonus;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.contents.DexRepository;
import java.util.*;
@Component
@RequiredArgsConstructor
public class StatCalculator {

    private final UserStatRepository userStatRepository;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;

    public BattleStatDto calculateBattleStat(Long userId) {
        // 기본 스탯
        UserStat stat = userStatRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("기본 스탯이 없습니다."));
        int baseHp = stat.getUserHp();
        int basePower = stat.getUserPower();
        int baseSpeed = stat.getUserSpeed();

        // 도감 스탯
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 상세정보가 없습니다."));
        Long equippedDexId = userDtl.getCharacterDexId();
        Dex dex = dexRepository.findById(equippedDexId).orElse(null);

        int dexHp = 0, dexPower = 0, dexSpeed = 0;
        if (dex != null) {
            dexHp = dex.getDexHp();
            dexPower = dex.getDexPower();
            dexSpeed = dex.getDexSpeed();
        }

        // 장비 스탯
        List<UserEquipment> battleEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.BATTLE);
        int equipHp = 0, equipPower = 0, equipSpeed = 0;
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (UserEquipment eq : battleEquipments) {
            Item item = eq.getItem();
            itemDtoList.add(toItemDto(item));
            ItemStatBonus bonus = itemStatBonusRepository.findById(eq.getItemId()).orElse(null);
            if (bonus != null) {
                equipHp += bonus.getBonusHp();
                equipPower += bonus.getBonusPower();
                equipSpeed += bonus.getBonusSpeed();
            }
        }

        return new BattleStatDto(
                baseHp + dexHp + equipHp,
                basePower + dexPower + equipPower,
                baseSpeed + dexSpeed + equipSpeed,
                itemDtoList
        );
    }

    private ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getRank(),
                item.getRarity(),
                item.getItemType().name(),
                item.getEquipSlot().name(),
                item.getIconPath()
        );
    }
}
