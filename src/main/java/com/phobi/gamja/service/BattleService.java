package com.phobi.gamja.service;

import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.user.BattleStatDto;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.contents.MonsterMap;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.contents.MonsterMapRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.user.UserDexStatRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterMapRepository monsterMapRepository;
    private final ItemRepository itemRepository;
    private final UserDexStatRepository userDexStatRepository;

    public GamJaResponse getMapList(HttpServletRequest request) {
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

            List<Item> uniqueDrops = collectUniqueDropItems(monsterDtos);
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

        return GamJaResponse.success("맵 리스트 조회 성공", result);
    }

    public GamJaResponse getUserBattleStat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return GamJaResponse.fail("장착한 캐릭터가 없습니다.");
        }

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat userDexStat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        BattleStatDto userBattleDto = statCalculator.calculateBattleStat(userId);

        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", userDtl.getUser().getUsername());
        userInfo.put("charImage", userDtl.getCharacterImage());
        userInfo.put("dexName", userDexStat.getDex().getName());
        userInfo.put("lv", userDexStat.getLevel());
        userInfo.put("xp", userDexStat.getXp());
        userInfo.put("maxExp", userDexStat.getMaxExp());
        userInfo.put("attribute", userDexStat.getDex().getAttribute());
        userInfo.put("power", userBattleDto.getTotalPower());
        userInfo.put("hp", userBattleDto.getTotalHp());
        userInfo.put("speed", userBattleDto.getTotalSpeed());

        return GamJaResponse.success("유저 스탯 조회 성공", userInfo);
    }

    public GamJaResponse getMonstersByMap(Long mapId) {
        Optional<MonsterMap> mapOpt = monsterMapRepository.findById(mapId);
        if (mapOpt.isEmpty()) {
            return GamJaResponse.fail("맵을 찾을 수 없습니다.");
        }

        List<Monster> monsters = monsterRepository.findByMapAndEnabledIsTrue(mapOpt.get());
        List<MonsterDto> monsterDtos = monsters.stream()
                .map(this::toMonsterDtoWithDropItems)
                .collect(Collectors.toList());

        return GamJaResponse.success("몬스터 전체 조회 성공", monsterDtos);
    }

    private List<Item> collectUniqueDropItems(List<MonsterDto> monsters) {
        return monsters.stream()
                .flatMap(m -> m.getDropItems().stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a),
                        m -> new ArrayList<>(m.values())
                ));
    }

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
