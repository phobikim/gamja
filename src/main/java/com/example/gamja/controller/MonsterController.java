package com.example.gamja.controller;

import com.example.gamja.dto.MonsterDto;
import com.example.gamja.entity.Dex;
import com.example.gamja.entity.Item;
import com.example.gamja.entity.Monster;
import com.example.gamja.entity.UserDtl;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import com.example.gamja.util.CommonUtil;
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
    private final UserDtlRepository userDtlRepository;
    private final DexRepository dexRepository;
    private final MonsterRepository monsterRepository;
    private final ItemRepository itemRepository;

    /** 1. 유저 스탯 + 프로필 정보 조회 **/
    @GetMapping("/user-stat/{userId}")
    public ResponseEntity<GamJaResponse> getUserStat(@PathVariable Long userId, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return ResponseEntity.ok(GamJaResponse.fail("장착한 캐릭터가 없습니다."));
        }

        Dex dex = dexRepository.findById(dexId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 도감 정보가 없습니다."));

        // 이미지 보정 처리
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", userDtl.getUser().getUsername());
        userInfo.put("lv", userDtl.getLevel());
        userInfo.put("xp", userDtl.getXp());
        userInfo.put("charImage", userDtl.getCharacterImage());
        userInfo.put("power", dex.getDexPower());
        userInfo.put("hp", dex.getDexHp());

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
