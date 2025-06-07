package com.phobi.gamja.controller;


import com.phobi.gamja.entity.dex.DexAttribute;
import com.phobi.gamja.entity.dex.DexRarityStat;
import com.phobi.gamja.entity.user.UserDex;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.UserDexRepository;
import com.phobi.gamja.repository.user.UserDexStatRepository;
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

    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final ItemRepository itemRepository;
    private final MonsterRepository monsterRepository;

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
                    m.put("acquireCondition", dex.getAcquireCondition());

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
                    }

                    return m;
                })
                .sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());

        // 2. item
        List<Map<String, Object>> itemResult = itemRepository.findAll().stream()
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", item.getId());
                    m.put("name", item.getName());
                    m.put("description", item.getDescription());
                    m.put("rank", item.getRank());
                    m.put("rarity", item.getRarity());
                    m.put("imagePath", item.getIconPath());
                    return m;
                }).sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());

        // 3. monster
        List<Map<String, Object>> monsterResult = monsterRepository.findAll().stream()
                .map(mon -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", mon.getId());
                    m.put("name", mon.getName());
                    m.put("description", mon.getDesc());
                    m.put("rank", mon.getRank());
                    m.put("rarity", mon.getRarity());
                    m.put("imagePath", mon.getImagePath());
                    return m;
                }).sorted(Comparator.comparing(m -> RARITY_ORDER.getOrDefault(String.valueOf(m.get("rarity")), 99)))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("dexList", dexResult);
        data.put("itemList", itemResult);
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
