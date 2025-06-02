package com.phobi.gamja.util;

import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.item.ItemSkillBonus;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
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

    private final UserDexStatRepository userDexStatRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;

    public BattleStatDto calculateBattleStat(Long userId) {
        // 1. 유저 상세정보 + 착용 캐릭터 ID
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 상세정보가 없습니다."));
        Long equippedDexId = userDtl.getCharacterDexId();

        // 2. 캐릭터별 스탯 (user_dex_stat)
        UserDexStatId statId = new UserDexStatId(userId, equippedDexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new RuntimeException("캐릭터 스탯 정보가 없습니다."));

        int baseHp = stat.getHp();
        int basePower = stat.getPower();
        int baseSpeed = stat.getSpeed();

        // 3. 도감 기본값 (dex table)
        Dex dex = dexRepository.findById(equippedDexId).orElse(null);
        int dexHp = 0, dexPower = 0, dexSpeed = 0;
        if (dex != null) {
            dexHp = dex.getDexHp();
            dexPower = dex.getDexPower();
            dexSpeed = dex.getDexSpeed();
        }
        // 장비 스탯
        List<UserEquipment> battleEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.EQUIP_BATTLE);
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

    public LifeStatDto calculateLifeSkill(Long userId) {
        // 기본 스킬 레벨
        Map<String, Integer> baseSkillMap = new HashMap<>();
        List<UserSkill> skillList = userSkillRepository.findByUserId(userId);
        for (UserSkill skill : skillList) {
            baseSkillMap.put(skill.getSkillType().name(), skill.getLevel());
        }
        int baseFishing = baseSkillMap.getOrDefault("FISHING", 1);
        int baseMining = baseSkillMap.getOrDefault("MINING", 1);
        int baseWoodcutting = baseSkillMap.getOrDefault("WOODCUTTING", 1);
        int baseGathering = baseSkillMap.getOrDefault("GATHERING", 1);
        int baseMaking = baseSkillMap.getOrDefault("MAKING", 1);

        // 장비 스킬 보너스
        List<UserEquipment> gatherEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.EQUIP_GATHER);
        int equipFishing = 0, equipMining = 0, equipWoodcutting = 0, equipGathering = 0, equipMaking = 0;
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (UserEquipment eq : gatherEquipments) {
            Item item = eq.getItem();
            itemDtoList.add(toItemDto(item));
            ItemSkillBonus bonus = itemSkillBonusRepository.findById(eq.getItemId()).orElse(null);
            if (bonus != null) {
                equipFishing += bonus.getFishing();
                equipMining += bonus.getMining();
                equipWoodcutting += bonus.getWoodcutting();
                equipGathering += bonus.getGathering();
                equipMaking += bonus.getMaking();
            }
        }

        return new LifeStatDto(
                baseFishing + equipFishing,
                baseMining + equipMining,
                baseWoodcutting + equipWoodcutting,
                baseGathering + equipGathering,
                baseMaking + equipMaking,
                itemDtoList);
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
