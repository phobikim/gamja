package com.phobi.gamja.controller;


import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.dex.DexAttribute;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemSkillBonus;
import com.phobi.gamja.entity.item.ItemStatBonus;
import com.phobi.gamja.entity.user.UserDex;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.user.UserDexRepository;
import com.phobi.gamja.repository.user.UserDexStatRepository;
import com.phobi.gamja.service.BattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dex")
public class DexController {
    private final BattleService battleService;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final ItemRepository itemRepository;
    private final MonsterRepository monsterRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getDexMeta(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        // 1. dex
        List<Dex> dexList = dexRepository.findAllEnabledForUser(); // user_flag = true
        List<UserDex> ownedDexList = userDexRepository.findByUserId(userId);
        Set<Long> ownedDexIds = ownedDexList.stream()
                .map(ud -> ud.getDex().getId())
                .collect(Collectors.toSet());
        List<UserDexStat> statList = userDexStatRepository.findByUser_Id(userId);

        Map<Long, UserDexStat> statMap = statList.stream()
                .collect(Collectors.toMap(stat -> stat.getDex().getId(), stat -> stat));

        List<Map<String, Object>> dexResult = dexList.stream()
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

                    DexAttribute attr = dex.getAttribute();
                    m.put("attribute", attr.getName());
                    m.put("attributeIconPath", attr.getIconPath());

                    DexRarityStat rarityStat = dex.getRarity();
                    m.put("rarity", rarityStat.getRarity().name());
                    m.put("rarityLabel", rarityStat.getBonusDescription());


                    if (isOwned) {
                        UserDexStat stat = statMap.get(dexId);
                        m.put("affinity", stat != null ? stat.getAffinity() : 0);
                        m.put("level", stat != null ? stat.getLevel() : 1);
                        m.put("currentXp", stat != null ? stat.getXp() : 0);
                        m.put("maxXp", stat != null ? stat.getMaxExp() : 100);
                        m.put("basePower", rarityStat.getBasePower());
                        m.put("baseHp", rarityStat.getBaseHp());
                        m.put("baseSpeed", rarityStat.getBaseSpeed());
                    }

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());

        // 2. item
        List<Item> allItems = itemRepository.findAll();

        // 장비 타입 기준
        Set<Item.ItemType> battleTypes = Set.of(Item.ItemType.EQUIP_BATTLE);
        Set<Item.ItemType> lifeTypes = Set.of(Item.ItemType.EQUIP_GATHER);

        // 전투 장비
        List<Map<String, Object>> battleEquipItems = allItems.stream()
                .filter(item -> battleTypes.contains(item.getItemType()))
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

                    // 전투 스탯 추가
                    ItemStatBonus stat = itemStatBonusRepository.findById(item.getId()).orElse(null);
                    m.put("baseHp", stat != null ? stat.getBonusHp() : 0);
                    m.put("basePower", stat != null ? stat.getBonusPower() : 0);
                    m.put("baseSpeed", stat != null ? stat.getBonusSpeed() : 0);

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());

        // 생활 장비
        List<Map<String, Object>> lifeEquipItems = allItems.stream()
                .filter(item -> lifeTypes.contains(item.getItemType()))
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", item.getId());
                    m.put("name", item.getName());
                    m.put("description", item.getDescription());
                    m.put("rank", item.getRank());
                    m.put("rarity", item.getRarity());
                    m.put("imagePath", item.getIconPath());
                    m.put("condition", item.getCondition());
                    m.put("equipSlot", item.getEquipSlot());

                    // 생활 스킬 보너스 추가
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

        Set<Item.ItemType> nonEquipTypes = Set.of(
                Item.ItemType.GATHER_MATERIAL,
                Item.ItemType.CRAFT_MATERIAL,
                Item.ItemType.COMPOSITE,
                Item.ItemType.DROP
                // POTION은 EQUIP_POTION 따로 있으면 제외
        );
        // 아이템
        List<Map<String, Object>> nonEquipItems = allItems.stream()
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


        // 3. monster
        List<Monster> monsters = monsterRepository.findAll();
        List<Map<String, Object>> monsterResult = monsters.stream()
                .map(mon -> {
                    MonsterDto dto = battleService.toMonsterDtoWithDropItems(mon); // ✅ 재사용

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", dto.getId());
                    m.put("name", dto.getName());
                    m.put("description", dto.getDesc());
                    m.put("rank", dto.getRank());
                    m.put("rarity", mon.getRarity()); // 이건 DTO에 없으니까 원본에서
                    m.put("basePower", dto.getMonsterPower());
                    m.put("baseHp", dto.getMonsterHp());
                    m.put("imagePath", dto.getImagePath());
                    m.put("condition", mon.getCondition());

                    // ✅ drop item list 구성
                    List<Map<String, Object>> dropList = dto.getDropItems().stream()
                            .map(item -> {
                                Map<String, Object> d = new HashMap<>();
                                d.put("id", item.getId());
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



        Map<String, Object> data = new HashMap<>();
        data.put("dexList", dexResult);
        data.put("battleEquipItemList", battleEquipItems);
        data.put("lifeEquipItemsList", lifeEquipItems);
        data.put("itemList", nonEquipItems);
        data.put("monsterList", monsterResult);

        return ResponseEntity.ok(GamJaResponse.success("도감 메타데이터 조회 완료", data));
    }

    private static final Map<String, Integer> RARITY_ORDER = Map.of(
            "COMMON", 1,
            "UNCOMMON", 2,
            "RARE", 3,
            "EPIC", 4,
            "LEGENDARY", 5
    );
}
