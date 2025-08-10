package com.phobi.gamja.controller;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterMap;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.BattleService;
import com.phobi.gamja.service.BossBattleService;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battle/boss")
@RequiredArgsConstructor
public class BossBattleController {
    private final BattleService battleService;
    private final BossBattleService bossBattleService;
    private final CommonUtil commonUtil;

    @PostMapping("/start-boss-battle")
    public ResponseEntity<GamJaResponse> startBossBattle(HttpSession session) {
        GamJaResponse response = bossBattleService.startBossBattle(session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/player-attack")
    public ResponseEntity<GamJaResponse> bossPlayerAttack(HttpSession session) {
        GamJaResponse response = bossBattleService.playerAttack(session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/monster-attack")
    public ResponseEntity<GamJaResponse> bossMonsterAttack(HttpSession session) {
        GamJaResponse response = bossBattleService.bossTurn(session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end-boss-battle")
    public GamJaResponse endBossBattle(HttpSession session,
                                       @RequestParam(value = "outcome", required = false) String outcome) {
        return bossBattleService.endBossBattle(session, outcome);
    }
}
