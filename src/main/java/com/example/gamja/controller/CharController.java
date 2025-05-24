package com.example.gamja.controller;

import com.example.gamja.dto.UserCharInfoDto;
import com.example.gamja.dto.UserDtlDto;
import com.example.gamja.entity.Dex;
import com.example.gamja.entity.UserDtl;
import com.example.gamja.entity.UserInventory;
import com.example.gamja.entity.UserSkill;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import com.example.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.gamja.config.XpCofig.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/char")
public class CharController {
    private final CommonUtil commonUtil;
    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;

    @ResponseBody
    @GetMapping("/{userId}")
    public ResponseEntity<GamJaResponse> getCharInfo(@PathVariable Long userId, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("접근 권한이 없습니다."));
        }

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage);

        UserCharInfoDto result = new UserCharInfoDto(userDtl);
        result.setTitle(result.getTitleByLevel(userDtl.getLevel()));
        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }

    @PostMapping("/setImage")
    public ResponseEntity<GamJaResponse> setCharacterImage(@RequestBody Map<String, Long> payload, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Long dexId = payload.get("dexId");

        // 1. 감자 획득 여부 확인
        boolean owned = userDexRepository.existsByUserIdAndDexId(userId, dexId);
        if (!owned) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("미획득한 감자는 설정할 수 없습니다."));
        }

        // 2. 대표 감자 설정
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        userDtl.setCharacterDexId(dexId);
        userDtlRepository.save(userDtl);

        // 3. 캐릭터 이미지 경로 재계산
        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage); // 프론트에서 바로 쓸 수 있게 임시 세팅

        UserDtlDto dto = UserDtlDto.builder()
                .id(userDtl.getId())
                .usernickname(userDtl.getUsernickname())
                .characterImage(finalImage)
                .characterDexId(userDtl.getCharacterDexId())
                .level(userDtl.getLevel())
                .xp(userDtl.getXp())
                .build();

        return ResponseEntity.ok(GamJaResponse.success("대표 감자가 설정되었습니다.", dto));
    }

}
