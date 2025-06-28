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
public class BattleController {

    private final BattleService battleService;

    /** 맵 리스트 조회 **/
    @GetMapping("/map-list")
    public ResponseEntity<GamJaResponse> getMapList(HttpServletRequest request) {
        GamJaResponse response = battleService.getMapList(request);
        return ResponseEntity.ok(response);
    }

    /* 전투 시작
     *  세션 저장
     *  */
    @PostMapping("/start-battle")
    public ResponseEntity<GamJaResponse> startBattle(@RequestBody Map<String, Object> request, HttpSession session) {
        GamJaResponse response = battleService.startBattle(request, session);
        return ResponseEntity.ok(response);
    }

    /* 전투 진행
    *  - player 공격 turn
    * */
    @PostMapping("/player-attack")
    public ResponseEntity<GamJaResponse> playerAttack(HttpSession session) {
        GamJaResponse response = battleService.playerAttack(session);
        return ResponseEntity.ok(response);
    }

    /* 전투 진행
     *  - monster 공격 turn
     * */
    @PostMapping("/monster-attack")
    public ResponseEntity<GamJaResponse> monsterAttack(HttpSession session) {
        GamJaResponse response = battleService.monsterAttack(session);
        return ResponseEntity.ok(response);
    }


    /* 전투 완료
     *  세션 초기화, 아이템 획득 처리
     *  */
    @PostMapping("/end-battle")
    public ResponseEntity<GamJaResponse> endBattle(HttpSession session) {
        GamJaResponse response = battleService.endBattle(session);
        return ResponseEntity.ok(response);
    }

    /** 물약 사용 처리 **/
    @PostMapping("/use-potion")
    public ResponseEntity<GamJaResponse> usePotion(HttpServletRequest request) {
        GamJaResponse response = battleService.usePotion(request);
        return ResponseEntity.ok(response);
    }

}
