package com.phobi.gamja.service;

import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.user.LifeStatDto;
import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.dex.*;
import com.phobi.gamja.entity.item.*;
import com.phobi.gamja.entity.title.Title;
import com.phobi.gamja.entity.title.TitleCondition;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.item.*;
import com.phobi.gamja.repository.title.TitleEffectRepository;
import com.phobi.gamja.repository.title.TitleRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DexService {

    private final BattleService battleService;
    private final StatCalculator statCalculator;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final ItemRepository itemRepository;
    private final MonsterRepository monsterRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;
    //칭호 시스템
    private final TitleRepository titleRepository;
    private final TitleEffectRepository titleEffectRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;

    private static final Map<String, Integer> RARITY_ORDER = Map.of(
            "COMMON", 1,
            "UNCOMMON", 2,
            "RARE", 3,
            "EPIC", 4,
            "LEGENDARY", 5
    );

    public GamJaResponse getDexMeta(Long userId) {
        Map<String, Object> data = new HashMap<>();

        data.put("dexList", buildDexList(userId));
        data.put("battleEquipItemList", buildBattleItems());
        data.put("lifeEquipItemsList", buildLifeItems());
        data.put("itemList", buildNonEquipItems());
        data.put("monsterList", buildMonsterList());
        data.put("equippedDexId", getEquippedDexId(userId));
        data.put("titleList", buildTitleList(userId));

        return GamJaResponse.success("도감 메타데이터 조회 완료", data);
    }

    private List<Map<String, Object>> buildDexList(Long userId) {
        List<Dex> dexList = dexRepository.findByUseFlagTrue();
        List<UserDex> ownedDexList = userDexRepository.findByUserId(userId);
        Set<Long> ownedDexIds = ownedDexList.stream()
                .map(ud -> ud.getDex().getId())
                .collect(Collectors.toSet());

        List<UserDexStat> statList = userDexStatRepository.findByUser_Id(userId);
        Map<Long, UserDexStat> statMap = statList.stream()
                .collect(Collectors.toMap(stat -> stat.getDex().getId(), stat -> stat));

        return dexList.stream()
                .map(dex -> {
                    Map<String, Object> m = new HashMap<>();
                    Long dexId = dex.getId();
                    boolean isOwned = ownedDexIds.contains(dexId);

                    m.put("id", dexId);
                    m.put("name", dex.getName());
                    m.put("description", dex.getDescription());
                    m.put("owned", isOwned);
                    m.put("imagePath", dex.getImage());
                    m.put("condition", dex.getCondition());
                    m.put("attribute", dex.getAttribute().getName());
                    m.put("attributeIconPath", dex.getAttribute().getIconPath());

                    DexRarityStat rarityStat = dex.getRarity();
                    m.put("rarity", rarityStat.getRarity().name());
                    m.put("rarityLabel", rarityStat.getBonusDescription());

                    if (isOwned) {
                        UserDexStat stat = statMap.get(dexId);
                        m.put("affinity", stat != null ? stat.getAffinity() : 0);
                        m.put("level", stat != null ? stat.getLevel() : 1);
                        m.put("currentXp", stat != null ? stat.getXp() : 0);
                        m.put("maxXp", stat != null ? stat.getMaxExp() : 100);
                        m.put("basePower", stat != null ? rarityStat.getBasePower() + stat.getPower() : rarityStat.getBasePower());
                        m.put("baseHp", stat != null ? rarityStat.getBaseHp() + stat.getHp() : rarityStat.getBaseHp());
                        m.put("baseSpeed", stat != null ? rarityStat.getBaseSpeed() + stat.getSpeed() : rarityStat.getBaseSpeed());
                    }

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());
    }

    private Long getEquippedDexId(Long userId) {
        return userDtlRepository.findById(userId)
                .map(UserDtl::getCharacterDexId)
                .orElse(null);
    }

    private List<Map<String, Object>> buildBattleItems() {
        List<Item> allItems = itemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getItemType() == Item.ItemType.EQUIP_BATTLE)
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", item.getId());
                    m.put("name", item.getName());
                    m.put("description", item.getDescription());
                    m.put("rank", item.getRank());
                    m.put("rarity", item.getRarity());
                    m.put("imagePath", item.getIconPath());
                    m.put("equipSlot", item.getEquipSlot());
                    m.put("condition", item.getCondition());

                    ItemStatBonus stat = itemStatBonusRepository.findById(item.getId()).orElse(null);
                    m.put("baseHp", stat != null ? stat.getBonusHp() : 0);
                    m.put("basePower", stat != null ? stat.getBonusPower() : 0);
                    m.put("baseSpeed", stat != null ? stat.getBonusSpeed() : 0);

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildLifeItems() {
        List<Item> allItems = itemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getItemType() == Item.ItemType.EQUIP_GATHER)
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", item.getId());
                    m.put("name", item.getName());
                    m.put("description", item.getDescription());
                    m.put("rank", item.getRank());
                    m.put("rarity", item.getRarity());
                    m.put("imagePath", item.getIconPath());
                    m.put("equipSlot", item.getEquipSlot());
                    m.put("condition", item.getCondition());

                    ItemSkillBonus bonus = itemSkillBonusRepository.findById(item.getId()).orElse(null);
                    m.put("fishing", bonus != null ? bonus.getFishing() : 0);
                    m.put("mining", bonus != null ? bonus.getMining() : 0);
                    m.put("woodcutting", bonus != null ? bonus.getWoodcutting() : 0);
                    m.put("gathering", bonus != null ? bonus.getGathering() : 0);
                    m.put("making", bonus != null ? bonus.getMaking() : 0);

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildNonEquipItems() {
        List<Item> allItems = itemRepository.findAll();
        Set<Item.ItemType> nonEquipTypes = Set.of(
                Item.ItemType.GATHER_MATERIAL,
                Item.ItemType.CRAFT_MATERIAL,
                Item.ItemType.COMPOSITE,
                Item.ItemType.DROP
        );

        return allItems.stream()
                .filter(item -> nonEquipTypes.contains(item.getItemType()))
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", item.getId());
                    m.put("name", item.getName());
                    m.put("description", item.getDescription());
                    m.put("rank", item.getRank());
                    m.put("rarity", item.getRarity());
                    m.put("imagePath", item.getIconPath());
                    m.put("condition", item.getCondition());
                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMonsterList() {
        List<Monster> monsters = monsterRepository.findAll();
        return monsters.stream()
                .map(mon -> {
                    MonsterDto dto = battleService.toMonsterDtoWithDropItems(mon);
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", dto.getId());
                    m.put("name", dto.getName());
                    m.put("description", dto.getDesc());
                    m.put("rank", dto.getRank());
                    m.put("rarity", mon.getRarity());
                    m.put("basePower", dto.getMonsterPower());
                    m.put("baseHp", dto.getMonsterHp());
                    m.put("imagePath", dto.getImagePath());
                    m.put("condition", mon.getCondition());

                    List<Map<String, Object>> dropList = dto.getDropItems().stream()
                            .map(item -> {
                                Map<String, Object> d = new HashMap<>();
                                d.put("id", item.getItemId());
                                d.put("name", item.getName());
                                d.put("rarity", item.getRarity());
                                d.put("imagePath", item.getIconPath());
                                return d;
                            }).collect(Collectors.toList());
                    m.put("dropItemList", dropList);

                    return m;
                })
                .sorted(Comparator
                        .comparing((Map<String, Object> m) -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99))
                        .thenComparing(m -> ((Number) m.get("basePower")).intValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTitleList(Long userId) {
        List<Title> titles = titleRepository.findAll();
        List<UserTitle> userTitles = userTitleRepository.findByIdUserId(userId);
        List<UserCounterDetail> counterDetails = userCounterDetailRepository.findByUserId(userId);

        Set<Long> ownedTitleIds = userTitles.stream()
                .map(ut -> ut.getTitle().getId())
                .collect(Collectors.toSet());

        Long equippedTitleId = userTitles.stream()
                .filter(UserTitle::isEquipped)
                .map(ut -> ut.getTitle().getId())
                .findFirst()
                .orElse(null);

        Map<String, Integer> counterMap = counterDetails.stream()
                .collect(Collectors.toMap(
                        c -> c.getCounterType().name() + "_" + c.getTargetId(),
                        UserCounterDetail::getCounterValue
                ));

        // 생활 스탯 미리 계산 (모든 타이틀에 대해 한 번만)
        LifeStatDto lifeStat = statCalculator.calculateLifeSkill(userId);

        return titles.stream().map(title -> {
            Map<String, Object> t = new HashMap<>();
            Long titleId = title.getId();

            List<Map<String, Object>> conditionList = new ArrayList<>();
            boolean achieved = true;
            int minProgress = 100;

            if (title.getCounterType() != CounterType.NONE) {
                List<TitleCondition> conditions = title.getConditions();
                achieved = true;
                minProgress = Integer.MAX_VALUE;

                for (TitleCondition cond : conditions) {
                    int current;
                    boolean pass;
                    String targetName;

                    if (title.getCounterType() == CounterType.LIFE_ACTION) {
                        // ✅ 생활 레벨 기반 조건
                        current = switch (cond.getLifeType()) {
                            case FISHING -> lifeStat.getFishing().getTotal();
                            case MINING -> lifeStat.getMining().getTotal();
                            case WOODCUTTING -> lifeStat.getWoodcutting().getTotal();
                            case GATHERING -> lifeStat.getGathering().getTotal();
                            case MAKING -> lifeStat.getMaking().getTotal();
                            default -> 0;
                        };
                        pass = current >= cond.getRequiredCount();
                        targetName = switch (cond.getLifeType()) {
                            case FISHING -> "낚시 레벨";
                            case MINING -> "채광 레벨";
                            case WOODCUTTING -> "벌목 레벨";
                            case GATHERING -> "채집 레벨";
                            case MAKING -> "제작 레벨";
                            default -> "???";
                        };
                    } else {
                        // ✅ 일반 카운터 기반 조건
                        String key = title.getCounterType().name() + "_" + cond.getTargetId();
                        current = counterMap.getOrDefault(key, 0);
                        pass = current >= cond.getRequiredCount();

                        targetName = switch (title.getCounterType()) {
                            case MONSTER_KILL -> {
                                Monster m = monsterRepository.findById(cond.getTargetId()).orElse(null);
                                yield m != null ? m.getName() : "???";
                            }
                            case ITEM_CRAFT -> {
                                Item item = itemRepository.findById(cond.getTargetId()).orElse(null);
                                yield item != null ? item.getName() : "???";
                            }
                            case CHARACTER_DRAW -> {
                                Dex dex = dexRepository.findById(cond.getTargetId()).orElse(null);
                                yield dex != null ? dex.getName() : "???";
                            }
                            case QUEST_COMPLETE -> "퀘스트 수행";
                            default -> "???";
                        };
                    }

                    achieved &= pass;
                    int progress = current * 100 / Math.max(cond.getRequiredCount(), 1);
                    minProgress = Math.min(minProgress, progress);

                    Map<String, Object> condMap = new HashMap<>();
                    condMap.put("targetId", cond.getTargetId());
                    condMap.put("targetName", targetName);
                    condMap.put("requiredCount", cond.getRequiredCount());
                    condMap.put("currentCount", current);
                    condMap.put("achieved", pass);
                    conditionList.add(condMap);
                }
            } else {
                // ✅ 조건 없음
                achieved = true;
                minProgress = 100;

                Map<String, Object> condMap = new HashMap<>();
                condMap.put("targetId", null);
                condMap.put("targetName", "조건 없음");
                condMap.put("requiredCount", 0);
                condMap.put("currentCount", 0);
                condMap.put("achieved", true);
                conditionList.add(condMap);
            }

            t.put("id", titleId);
            t.put("name", title.getName());
            t.put("description", title.getDescription());
            t.put("rarity", title.getRarity().name());
            t.put("iconPath", title.getIconPath());
            t.put("counterType", title.getCounterType().name());
            t.put("owned", ownedTitleIds.contains(titleId));
            t.put("equipped", titleId.equals(equippedTitleId));
            t.put("achieved", achieved);
            t.put("progress", minProgress);
            t.put("conditions", conditionList);

            // 효과 정보
            List<Map<String, Object>> effectList = titleEffectRepository.findByTitleId(titleId).stream()
                    .map(effect -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("effectType", effect.getEffectType().name());
                        e.put("effectValue", effect.getEffectValue());
                        return e;
                    }).collect(Collectors.toList());
            t.put("effects", effectList);

            return t;
        }).collect(Collectors.toList());
    }
}
