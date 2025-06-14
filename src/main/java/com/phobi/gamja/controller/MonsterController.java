package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.BattleService;
import com.phobi.gamja.web.config.annotation.SanitizeInput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

}
