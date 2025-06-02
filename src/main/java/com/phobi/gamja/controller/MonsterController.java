package com.phobi.gamja.controller;

import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.user.BattleStatDto;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.contents.MonsterMap;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.MonsterMapRepository;
import com.phobi.gamja.repository.user.UserDexStatRepository;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class MonsterController {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterMapRepository monsterMapRepository;
    private final ItemRepository itemRepository;
    private final UserDexStatRepository userDexStatRepository;

    /** 1. 맵 선택 **/
    @GetMapping("/map-list")
    public ResponseEntity<GamJaResponse> mapList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        List<MonsterMap> maps = monsterMapRepository.findAll().stream()
                .filter(MonsterMap::isEnabled)
                .collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (MonsterMap map : maps) {
            List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(map);
            List<MonsterDto> monsterDtos = monsters.stream()
                    .map(this::toMonsterDtoWithDropItems)
                    .collect(Collectors.toList());

            List<Map<String, Object>> monsterList = monsters.stream()
                    .sorted(Comparator.comparingInt(Monster::getMonsterPower))
                    .map(monster -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", monster.getId());
                        m.put("name", monster.getName());
                        m.put("rank", monster.getRank());
                        m.put("monsterImg", monster.getImagePath());
                        m.put("desc", monster.getDesc());
                        return m;
                    })
                    .collect(Collectors.toList());

            //아이템 중복 제거
            List<Item> uniqueDrops = collectUniqueDropItems(monsterDtos);

            // 희귀도 기준 정렬
            uniqueDrops.sort(Comparator.comparing(Item::getRarity));
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("id", map.getId());
            mapData.put("name", map.getName());
            mapData.put("desc", map.getDesc());
            mapData.put("imagePath", map.getBackgroundImagePath());
            mapData.put("recommendedLevel", map.getRecommendedLevel());
            mapData.put("monsters", monsterList);
            mapData.put("rewards", uniqueDrops);
            result.add(mapData);

        }
        return ResponseEntity.ok(GamJaResponse.success("맵 리스트 조회 성공", result));
    }

    private List<Item> collectUniqueDropItems(List<MonsterDto> monsters) {
        return monsters.stream()
                .flatMap(m -> m.getDropItems().stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a),
                        m -> new ArrayList<>(m.values())
                ));
    }

    /** 2. 유저 스탯 + 프로필 정보 조회 **/
    @GetMapping("/user-stat")
    public ResponseEntity<GamJaResponse> getUserStat(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return ResponseEntity.ok(GamJaResponse.fail("장착한 캐릭터가 없습니다."));
        }

        // 🔸 캐릭터 스탯
        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat userDexStat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        // 🔸 전투 스탯 계산
        BattleStatDto result = statCalculator.calculateBattleStat(userId);

        // 🔸 이미지 경로 보정
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("dexName", userDexStat.getDex().getName());
        userInfo.put("name", userDtl.getUser().getUsername());
        userInfo.put("lv", userDexStat.getLevel());
        userInfo.put("xp", userDexStat.getXp());
        userInfo.put("maxExp", userDexStat.getMaxExp());
        userInfo.put("charImage", userDtl.getCharacterImage());
        userInfo.put("power", result.getTotalPower());
        userInfo.put("hp", result.getTotalHp());
        userInfo.put("speed", result.getTotalSpeed());
        userInfo.put("attribute", userDexStat.getDex().getAttribute());

        return ResponseEntity.ok(GamJaResponse.success("유저 스탯 조회 성공", userInfo));
    }

    /** 3. 몬스터 스탯 + 드랍 아이템 **/
    @GetMapping("/monster_stat")
    public ResponseEntity<GamJaResponse> getMonstersByMap(@RequestParam("mapId") Long mapId) {
        Optional<MonsterMap> mapOpt = monsterMapRepository.findById(mapId);
        if (mapOpt.isEmpty()) {
            return ResponseEntity.status(404).body(GamJaResponse.fail("맵을 찾을 수 없습니다."));
        }

        List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(mapOpt.get());
        List<MonsterDto> monsterDtos = monsters.stream()
                .map(this::toMonsterDtoWithDropItems)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("몬스터 전체 조회 성공", monsterDtos));
    }


    /** 몬스터 → DTO 변환 (드랍 포함) **/
    private MonsterDto toMonsterDtoWithDropItems(Monster monster) {
        MonsterDto dto = new MonsterDto();
        dto.setId(monster.getId());
        dto.setName(monster.getName());
        dto.setDesc(monster.getDesc());
        dto.setRank(monster.getRank());
        dto.setImagePath(monster.getImagePath());
        dto.setMonsterPower(monster.getMonsterPower());
        dto.setMonsterHp(monster.getMonsterHp());
        dto.setMonsterXp(monster.getMonsterXp());

        List<Item> dropItems = new ArrayList<>();
        if (monster.getDropItem1Id() != null) itemRepository.findById(monster.getDropItem1Id()).ifPresent(dropItems::add);
        if (monster.getDropItem2Id() != null) itemRepository.findById(monster.getDropItem2Id()).ifPresent(dropItems::add);
        if (monster.getDropItem3Id() != null) itemRepository.findById(monster.getDropItem3Id()).ifPresent(dropItems::add);
        if (monster.getDropItem4Id() != null) itemRepository.findById(monster.getDropItem4Id()).ifPresent(dropItems::add);
        if (monster.getDropItem5Id() != null) itemRepository.findById(monster.getDropItem5Id()).ifPresent(dropItems::add);

        dto.setDropItems(dropItems);
        return dto;
    }
}
