package com.phobi.gamja.controller;

import com.phobi.gamja.dto.battle.SkillResultDto;
import com.phobi.gamja.dto.dex.DexSkillDto;
import com.phobi.gamja.entity.battle.BattleSkill;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.BattleService;
import com.phobi.gamja.web.config.annotation.SanitizeInput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class MonsterController {

    private final BattleService battleService;

    /** 맵 리스트 조회 **/
    @GetMapping("/map-list")
    public ResponseEntity<GamJaResponse> getMapList(HttpServletRequest request) {
        GamJaResponse response = battleService.getMapList(request);
        return ResponseEntity.ok(response);
    }

    /** 유저 전투 스탯 조회 **/
    @GetMapping("/user-stat")
    public ResponseEntity<GamJaResponse> getUserBattleStat(HttpServletRequest request) {
        GamJaResponse response = battleService.getUserBattleStat(request);
        return ResponseEntity.ok(response);
    }

    /** 특정 맵의 몬스터 정보 조회 **/
    @GetMapping("/monster_stat")
    @SanitizeInput
    public ResponseEntity<GamJaResponse> getMonsterStatsByMap(@RequestParam("mapId") Long mapId) {
        GamJaResponse response = battleService.getMonstersByMap(mapId);
        return ResponseEntity.ok(response);
    }

    /** 물약 사용 처리 **/
    @PostMapping("/use-potion")
    public ResponseEntity<GamJaResponse> usePotion(HttpServletRequest request) {
        GamJaResponse response = battleService.usePotion(request);
        return ResponseEntity.ok(response);
    }

    /*스킬 정보 조회 */
    /**
     * GET /api/battle/{attribute}?type=BASIC
     */
    @GetMapping("/{attribute}")
    public ResponseEntity<GamJaResponse> getDexSkill(
            @PathVariable String attribute,
            @RequestParam("type") BattleSkill.Type type) {

        List<DexSkillDto> skills = battleService.getSkills(attribute, type);
        return ResponseEntity.ok(
                GamJaResponse.success("스킬 조회 완료", skills)
        );
    }

    /**
     * POST /api/battle/use-skill/{skillId}
     */
    @PostMapping("/use-skill/{skillId}")
    public ResponseEntity<GamJaResponse> useSkill(@PathVariable Long skillId) {
        SkillResultDto result = battleService.useSkill(skillId);
        return ResponseEntity.ok(GamJaResponse.success("스킬 사용 완료", result));
    }

}
