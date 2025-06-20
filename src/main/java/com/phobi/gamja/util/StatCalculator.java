package com.phobi.gamja.util;

import com.phobi.gamja.dto.battle.BattleStatDetailDto;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.item.ItemSkillBonus;
import com.phobi.gamja.entity.title.TitleEffect;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.title.TitleEffectRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.phobi.gamja.dto.item.ItemDto;
import com.phobi.gamja.dto.user.*;
import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemStatBonus;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.dex.DexRepository;
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
    private final UserTitleRepository userTitleRepository;
    private final TitleEffectRepository titleEffectRepository;
    public BattleStatDto calculateBattleStat(Long userId) {
        // 1. 유저 상세정보 + 착용 캐릭터 ID
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 상세정보가 없습니다."));
        Long equippedDexId = userDtl.getCharacterDexId();

        // 2. 착용 캐릭터의 stat (레벨업하면 증가)
        UserDexStatId statId = new UserDexStatId(userId, equippedDexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new RuntimeException("캐릭터 스탯 정보가 없습니다."));

        int userDexStatHp = stat.getHp();
        int userDexStatPower = stat.getPower();
        int userDexStatSpeed = stat.getSpeed();

        // 3. 랭크 별 기본 스탯 (랭크 별로 차이가 있음)
        Dex dex = dexRepository.findById(equippedDexId).orElse(null);
        int baseDexHp = 0, baseDexPower = 0, baseDexSpeed = 0;
        if (dex != null && dex.getRarity() != null) {
            DexRarityStat rarityStat = dex.getRarity(); // 이미 연관관계로 연결됨
            baseDexHp = rarityStat.getBaseHp();
            baseDexPower = rarityStat.getBasePower();
            baseDexSpeed = rarityStat.getBaseSpeed();
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

        // 4. 착용 칭호 스탯 추가
        UserTitle equippedTitle = userTitleRepository.findByIdUserId(userId).stream()
                .filter(UserTitle::isEquipped)
                .findFirst()
                .orElse(null);
        if (equippedTitle != null) {
            List<TitleEffect> effects = titleEffectRepository.findByTitleId(equippedTitle.getTitle().getId());
            for (TitleEffect effect : effects) {
                switch (effect.getEffectType()) {
                    case BONUS_ATTACK -> equipPower += effect.getEffectValue();
                    case BONUS_HP -> equipHp += effect.getEffectValue();
                }
            }
        }

        BattleStatDetailDto power = new BattleStatDetailDto(userDexStatPower, baseDexPower, equipPower);
        BattleStatDetailDto hp = new BattleStatDetailDto(userDexStatHp, baseDexHp, equipHp);
        BattleStatDetailDto speed = new BattleStatDetailDto(userDexStatSpeed, baseDexSpeed, equipSpeed);


        return new BattleStatDto(hp, power, speed, itemDtoList);
    }

    public LifeStatDto calculateLifeSkill(Long userId) {
        // 1. 유저 상세정보 + 착용 캐릭터 ID
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 상세정보가 없습니다."));
        Long equippedDexId = userDtl.getCharacterDexId();
        // 유저 스킬 레벨 (활동을 통해 스탯 증가)
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
        LifeStatDetailDto fishing = new LifeStatDetailDto(baseFishing, equipFishing);
        LifeStatDetailDto mining = new LifeStatDetailDto(baseMining, equipMining);
        LifeStatDetailDto woodCutting = new LifeStatDetailDto(baseWoodcutting, equipWoodcutting);
        LifeStatDetailDto gathering = new LifeStatDetailDto(baseGathering, equipGathering);
        LifeStatDetailDto making = new LifeStatDetailDto(baseMaking, equipMaking);
        return new LifeStatDto(
                fishing, mining, woodCutting, gathering, making,
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
