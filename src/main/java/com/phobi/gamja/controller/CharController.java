package com.phobi.gamja.controller;

import com.phobi.gamja.dto.item.ItemDto;
import com.phobi.gamja.dto.user.*;
import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/char")
public class CharController {
    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;

    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;

    private final UserDexStatRepository userDexStatRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;

    @ResponseBody
    @GetMapping("")
    public ResponseEntity<GamJaResponse> getCharInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("접근 권한이 없습니다."));
        }

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("착용 중인 캐릭터가 없습니다."));
        }

        // 🔸 현재 캐릭터 스탯 정보
        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        // 캐릭터 이미지 경로 업데이트
        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage);

        UserCharInfoDto result = new UserCharInfoDto(userDtl, stat);
        result.setTitle(result.getTitleByLevel(stat.getLevel()));
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }

    @GetMapping("/battle")
    public ResponseEntity<GamJaResponse> getBattleInfo (HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("접근 권한이 없습니다."));
        }
        BattleStatDto result = statCalculator.calculateBattleStat(userId);
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));

    }

    @GetMapping("/life")
    public ResponseEntity<GamJaResponse> getLifeInfo (HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("접근 권한이 없습니다."));
        }

        LifeStatDto result = statCalculator.calculateLifeSkill(userId);
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
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

    @PostMapping("/setDex")
    public ResponseEntity<GamJaResponse> setCharacterImage(@RequestBody Map<String, Long> payload, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("접근 권한이 없습니다."));
        }
        Long dexId = payload.get("dexId");

        // 1. 감자 보유 여부 확인
        boolean owned = userDexRepository.existsByUserIdAndDexId(userId, dexId);
        if (!owned) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("미획득한 감자는 설정할 수 없습니다."));
        }
        // 2. user_dtl 업데이트
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        userDtl.setCharacterDexId(dexId);
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl)); // 이미지 갱신
        userDtlRepository.save(userDtl);

        // 3. user_dex_stat 확인 or 생성
        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId).orElseGet(() -> {
            UserDexStat newStat = UserDexStat.builder()
                    .id(statId)
                    .user(userDtl.getUser())
                    .dex(Dex.builder().id(dexId).build()) // 또는 dexRepository.getReferenceById(dexId)
                    .level(1)
                    .xp(0)
                    .maxExp(100)
                    .power(1)
                    .hp(1)
                    .speed(1)
                    .build();
            return userDexStatRepository.save(newStat);
        });

        // 4. DTO 응답
        UserCharInfoDto dto = new UserCharInfoDto(userDtl, stat);
        return ResponseEntity.ok(GamJaResponse.success("대표 감자가 설정되었습니다.", dto));
    }

}
