package com.phobi.gamja.util;

import com.phobi.gamja.dto.battle.BattleStatDetailDto;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.battle.StatBonus;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.item.ItemSkillBonus;
import com.phobi.gamja.entity.title.TitleEffect;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.title.TitleEffectRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.service.CorpsTierService;
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

    private final CorpsTierService corpsTierService;

    private final UserDexStatRepository userDexStatRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;
    private final UserTitleRepository userTitleRepository;
    private final TitleEffectRepository titleEffectRepository;
    private final UserCorpsRepository userCorpsRepository;
    private final UserEnhancementRepository userEnhancementRepository;

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

        // 4. 장비 스탯
        List<UserEquipment> battleEquipments = userEquipmentRepository.findByUserIdAndType(userId, EquipmentType.EQUIP_BATTLE);
        int equipHp = 0, equipPower = 0, equipSpeed = 0;
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (UserEquipment eq : battleEquipments) {
            Item item = eq.getItem();
            UserEnhancementId enhanceId = new UserEnhancementId(userId, item.getId());
            UserEnhancement enhancement = userEnhancementRepository.findById(enhanceId).orElse(null);

            itemDtoList.add(toItemDto(item,enhancement));
            ItemStatBonus bonus = itemStatBonusRepository.findById(eq.getItemId()).orElse(null);
            if (bonus != null) {
                equipHp += bonus.getBonusHp();
                equipPower += bonus.getBonusPower();
                equipSpeed += bonus.getBonusSpeed();
            }
        }

        // 5. 착용 칭호 스탯 추가
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

        //6. 감자단 레벨 스탯
        UserCorps userCorps = userCorpsRepository.findById(userId).orElse(null);
        StatBonus corpsBonus = corpsTierService.calculateTierStatBonus(userCorps);
        // 7. BattleStatDetailDto 구성
        BattleStatDetailDto power = new BattleStatDetailDto(
                userDexStatPower,
                baseDexPower,
                equipPower,
                corpsBonus.power()
        );

        BattleStatDetailDto hp = new BattleStatDetailDto(
                userDexStatHp,
                baseDexHp,
                equipHp,
                corpsBonus.hp()
        );

        BattleStatDetailDto speed = new BattleStatDetailDto(
                userDexStatSpeed,
                baseDexSpeed,
                equipSpeed,
                corpsBonus.speed()
        );


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
            // 강화 이력 조회
            UserEnhancementId enhanceId = new UserEnhancementId(userId, item.getId());
            UserEnhancement enhancement = userEnhancementRepository.findById(enhanceId).orElse(null);

            // 아이템 DTO 구성 (강화 수치 포함)
            itemDtoList.add(toItemDto(item, enhancement));
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

    public ItemDto toItemDto(Item item, UserEnhancement enhancement) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .iconPath(item.getIconPath())
                .rarity(item.getRarity())
                .equipSlot(item.getEquipSlot().name())
                .enhancementLevel(enhancement != null ? enhancement.getEnhancementLevel() : 0)
                .enhancementXp(enhancement != null ? enhancement.getEnhancementXp() : 0)
                .build();
    }
}
