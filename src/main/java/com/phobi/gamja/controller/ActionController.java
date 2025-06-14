// ✨ 리팩토링 결과: ActionController의 전체 로직을 ActionService로 이동시킨 구조
package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.ActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/action")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @GetMapping("/{activityType}")
    public ResponseEntity<GamJaResponse> getActionsByCategory(@PathVariable String activityType, HttpSession session) {
        return actionService.getActionsByCategory(activityType, session);
    }

    @GetMapping("/card-event")
    public ResponseEntity<GamJaResponse> getCardEvents(@RequestParam String activity, @RequestParam int rank) {
        return actionService.getCardEvents(activity, rank);
    }

    @GetMapping("/{activityType}/{spotRank}")
    public ResponseEntity<GamJaResponse> getDropTable(@PathVariable String activityType, @PathVariable int spotRank, HttpSession session) {
        return actionService.getDropTable(activityType, spotRank, session);
    }

    /* 전투 컨텐츠 완료
    *  아이템 획득 처리
    *  */
    @PostMapping("/end-battle")
    public ResponseEntity<GamJaResponse> endBattle(@RequestBody Map<String, Object> request, HttpSession session) {
        return actionService.endBattle(session, request);
    }

    /* 생활 컨텐츠 완료
    *  아이템 획득 처리
    * */
    @PostMapping("/end-exploration")
    public ResponseEntity<GamJaResponse> endExploration(HttpSession session, @RequestBody Map<String, Object> request) {
        return actionService.endExploration(session, request);
    }

    // ✅ 칭호 획득
    @PostMapping("/title/claim")
    public ResponseEntity<GamJaResponse> claimTitle(@RequestBody Map<String, Object> request, HttpSession session) {
        return actionService.claimTitle(session, request);
    }

    // ✅ 칭호 착용
    @PostMapping("/title/equip")
    public ResponseEntity<GamJaResponse> equipTitle(@RequestBody Map<String, Object> request, HttpSession session) {
        return actionService.equipTitle(session, request);
    }
} 
