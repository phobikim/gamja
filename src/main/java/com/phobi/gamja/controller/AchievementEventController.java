package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.AchievementEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/achievement/event")
public class AchievementEventController {
    private final AchievementEventService achievementEventService;
    /**
     * 레벨업 이벤트
     * payload: { "characterId": 100, "currentLevel": 100 }
     */
    @PostMapping("/level-up")
    public ResponseEntity<GamJaResponse> onLevelUp(@RequestBody Map<String, Object> payload,
                                                   HttpSession session) {
        Long characterId = payload.get("characterId") == null ? null :
                ((Number) payload.get("characterId")).longValue();
        Integer currentLevel = payload.get("currentLevel") == null ? null :
                ((Number) payload.get("currentLevel")).intValue();

        return ResponseEntity.ok(
                achievementEventService.onLevelUp(characterId, currentLevel, session)
        );
    }

    /**
     * 몬스터 처치 이벤트
     * payload: { "monsterId": 33, "count": 1 }
     */
    @PostMapping("/monster-kill")
    public ResponseEntity<GamJaResponse> onMonsterKill(@RequestBody Map<String, Object> payload,
                                                       HttpSession session) {
        Long monsterId = payload.get("monsterId") == null ? null :
                ((Number) payload.get("monsterId")).longValue();
        Integer count = payload.get("count") == null ? null :
                ((Number) payload.get("count")).intValue();

        return ResponseEntity.ok(
                achievementEventService.onMonsterKill(monsterId, count, session)
        );
    }
}
