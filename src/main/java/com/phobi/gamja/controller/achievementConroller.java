package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.AchievementService;
import com.phobi.gamja.service.FameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/achievement")
public class achievementConroller {
    private final AchievementService achievementService;
    private final FameService fameService;

    /*
    * 카테고리별 대표 업적 리스트
    * */
    @GetMapping("/series")
    public ResponseEntity<GamJaResponse> listSeriesByCategory(
            @RequestParam(name = "category", required = false) String category,
            HttpSession session
    ) {
        return ResponseEntity.ok(achievementService.listSeriesByCategory(category, session));
    }

    /*
     * 카테고리별 전체 업적 시리즈 + 엔트리 리스트
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<GamJaResponse> listByCategory(
            @PathVariable String category,
            HttpSession session
    ) {
        return ResponseEntity.ok(achievementService.listByCategory(category, session));
    }

    /*
    * 업적 시리즈 상세 조회 (대표 업적 + 엔트리 + 리워드 + 유저 진행)
    * */
    @GetMapping("/series/{seriesKey}")
    public ResponseEntity<GamJaResponse> getSeries(
            @PathVariable String seriesKey,
            HttpSession session
    ) {
        return ResponseEntity.ok(achievementService.getSeries(seriesKey, session));
    }

    /*
     * 유저 명성 정보 조회 (없으면 신규 생성)
     */
    @GetMapping("/userFame")
    public ResponseEntity<GamJaResponse> getUserFame(HttpSession session) {
        return ResponseEntity.ok(fameService.getOrCreateUserFame(session));
    }
}
