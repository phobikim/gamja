package com.phobi.gamja.controller;

import com.phobi.gamja.dto.contents.MonsterDto;
import com.phobi.gamja.dto.user.BattleStatDto;
import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.message.GamJaResponse;
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

@RestController
@RequestMapping("/api/attack")
@RequiredArgsConstructor
public class MonsterController {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final MonsterRepository monsterRepository;
    private final ItemRepository itemRepository;
    private final UserDexStatRepository userDexStatRepository;

    /** 1. 유저 스탯 + 프로필 정보 조회 **/
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
        userInfo.put("name", userDtl.getUser().getUsername());
        userInfo.put("lv", userDexStat.getLevel());
        userInfo.put("xp", userDexStat.getXp());
        userInfo.put("maxExp", userDexStat.getMaxExp());
        userInfo.put("charImage", userDtl.getCharacterImage());
        userInfo.put("power", result.getTotalPower());
        userInfo.put("hp", result.getTotalHp());
        userInfo.put("speed", result.getTotalSpeed());

        return ResponseEntity.ok(GamJaResponse.success("유저 스탯 조회 성공", userInfo));
    }

    /** 2. 몬스터 전체 조회 (드랍 아이템 포함) **/
    @GetMapping("/monster_list")
    public ResponseEntity<GamJaResponse> getAllMonsters() {
        List<Monster> monsters = monsterRepository.findAll();

        List<MonsterDto> result = monsters.stream()
                .map(this::toMonsterDtoWithDropItems)
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("몬스터 전체 조회 성공", result));
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
