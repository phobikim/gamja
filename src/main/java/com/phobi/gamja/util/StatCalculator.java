package com.phobi.gamja.util;

import com.phobi.gamja.dto.battle.BattleStatDetailDto;
import com.phobi.gamja.dto.battle.BattleStatDto;
import com.phobi.gamja.dto.item.AlchemyOptionDto;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.battle.StatBonus;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.item.ItemAlchemyOption;
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

import java.math.BigDecimal;
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
    private final UserItemAlchemyOptionRepository userItemAlchemyOptionRepository;

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

        double totalCritRate = 0.0;
        double totalCritDmg = 0.0;
        double totalExpGain = 0.0;
        double totalGoldGain = 0.0;


        for (UserEquipment eq : battleEquipments) {
            Item item = eq.getItem();
            Long itemId = item.getId();

            // 1. 강화 정보 먼저 조회
            UserEnhancementId enhanceId = new UserEnhancementId(userId, itemId);
            UserEnhancement enhancement = userEnhancementRepository.findById(enhanceId).orElse(null);
            int bonusHp = 0, bonusPower = 0, bonusSpeed = 0;

            if (enhancement != null) {
                // 강화된 스탯 사용
                bonusHp = enhancement.getBonusHp();
                bonusPower = enhancement.getBonusPower();
                bonusSpeed = enhancement.getBonusSpeed();
            } else {
                // 기본 아이템 스탯 사용
                ItemStatBonus bonus = itemStatBonusRepository.findById(itemId).orElse(null);
                if (bonus != null) {
                    bonusHp = bonus.getBonusHp();
                    bonusPower = bonus.getBonusPower();
                    bonusSpeed = bonus.getBonusSpeed();
                }
            }

            //연금정보
            List<UserItemAlchemyOption> alchemyOptions = userItemAlchemyOptionRepository.findByUserIdAndItemId(userId, itemId);
            for (UserItemAlchemyOption opt : alchemyOptions) {
                if (opt.getValueType() == ItemAlchemyOption.ValueType.FLAT) {
                    switch (opt.getOptionType()) {
                        case HP -> bonusHp += opt.getOptionValue().intValue();
                        case ATTACK -> bonusPower += opt.getOptionValue().intValue();
                    }
                } else if (opt.getValueType() == ItemAlchemyOption.ValueType.PERCENT) {
                    switch (opt.getOptionType()) {
                        case CRIT_RATE -> totalCritRate += opt.getOptionValue().doubleValue();
                        case CRIT_DMG -> totalCritDmg += opt.getOptionValue().doubleValue();
                        case EXP_GAIN -> totalExpGain += opt.getOptionValue().doubleValue();
                        case GOLD_GAIN -> totalGoldGain += opt.getOptionValue().doubleValue();
                    }
                }
            }

            equipHp += bonusHp;
            equipPower += bonusPower;
            equipSpeed += bonusSpeed;

            itemDtoList.add(toItemDto(item, enhancement, alchemyOptions));
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

        int totalSpeed = userDexStatSpeed + baseDexSpeed + equipSpeed + corpsBonus.speed();
        double baseCritRate = totalSpeed / 10.0;
        double finalCritRate = totalCritRate + baseCritRate;
        if (finalCritRate < 10.0) {
            finalCritRate = 10.0;
        }

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


        return BattleStatDto.builder()
                .hp(hp)
                .power(power)
                .speed(speed)
                .equippedItems(itemDtoList)
                .critRate(finalCritRate)
                .critDmg(totalCritDmg)
                .expGain(totalExpGain)
                .goldGain(totalGoldGain)
                .build();
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
            Long userItemId = eq.getId();
            // 강화 이력 조회
            UserEnhancementId enhanceId = new UserEnhancementId(userId, item.getId());
            UserEnhancement enhancement = userEnhancementRepository.findById(enhanceId).orElse(null);
            List<UserItemAlchemyOption> alchemyOptions = userItemAlchemyOptionRepository.findByUserIdAndItemId(userId, userItemId);
            // 아이템 DTO 구성 (강화 ,연금은 없지만 포함)
            itemDtoList.add(toItemDto(item, enhancement, alchemyOptions));
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

    public ItemDto toItemDto(Item item, UserEnhancement enhancement, List<UserItemAlchemyOption> alchemyOptionList) {
        int bonusHp = 0, bonusPower = 0, bonusSpeed = 0;
        int enhancementLevel = 0, enhancementXp = 0;

        if (enhancement != null) {
            bonusHp = enhancement.getBonusHp();
            bonusPower = enhancement.getBonusPower();
            bonusSpeed = enhancement.getBonusSpeed();
            enhancementLevel = enhancement.getEnhancementLevel();
            enhancementXp = enhancement.getEnhancementXp();
        } else {
            ItemStatBonus bonus = itemStatBonusRepository.findById(item.getId()).orElse(null);
            if (bonus != null) {
                bonusHp = bonus.getBonusHp();
                bonusPower = bonus.getBonusPower();
                bonusSpeed = bonus.getBonusSpeed();
            }
        }

        List<AlchemyOptionDto> alchemyOptions = new ArrayList<>();
        Map<String, BigDecimal> specialOptions = new HashMap<>();

        if (alchemyOptionList != null) {
            for (UserItemAlchemyOption opt : alchemyOptionList) {
                alchemyOptions.add(new AlchemyOptionDto(
                        opt.getOptionType(),
                        opt.getValueType(),
                        opt.getOptionValue(),
                        opt.getDescription()
                ));

                if (opt.getValueType() == ItemAlchemyOption.ValueType.PERCENT) {
                    switch (opt.getOptionType()) {
                        case CRIT_RATE -> specialOptions.put("CRIT_RATE", opt.getOptionValue());
                        case CRIT_DMG -> specialOptions.put("CRIT_DMG", opt.getOptionValue());
                        case EXP_GAIN -> specialOptions.put("EXP_GAIN", opt.getOptionValue());
                        case GOLD_GAIN -> specialOptions.put("GOLD_GAIN", opt.getOptionValue());
                    }
                }
            }
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .rank(item.getRank())
                .rarity(item.getRarity())
                .itemType(item.getItemType().name())
                .equipSlot(item.getEquipSlot() != null ? item.getEquipSlot().name() : null)
                .iconPath(item.getIconPath())
                .enhancementLevel(enhancementLevel)
                .enhancementXp(enhancementXp)
                .bonusHp(bonusHp)
                .bonusPower(bonusPower)
                .bonusSpeed(bonusSpeed)
                .alchemyOptions(alchemyOptions)
                .specialOptions(specialOptions)
                .build();
    }

}
