package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.CharService;
import com.phobi.gamja.service.SkinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skin")
public class SkinController {
    private final SkinService skinService;

    /*
    * 배경 화면 list
    * */
    @GetMapping("/background/list")
    public ResponseEntity<GamJaResponse> backgroundList(HttpSession session) {
        return ResponseEntity.ok(skinService.getBackgroundList(session));
    }

    /*
     * 배경 화면 적용
     * */
    @PostMapping("/background/select")
    public ResponseEntity<GamJaResponse> setBackground(@RequestBody Map<String, Long> payload, HttpSession session) {
        return ResponseEntity.ok(skinService.setBackgroundList(payload, session));
    }

    /* 테두리 스킨 목록 */
    @GetMapping("/border/list")
    public ResponseEntity<GamJaResponse> borderList(HttpSession session) {
        return ResponseEntity.ok(skinService.getBorderList(session));
    }

    /* 테두리 스킨 적용 */
    @PostMapping("/border/select")
    public ResponseEntity<GamJaResponse> selectBorder(@RequestBody Map<String, Long> payload,
                                                      HttpSession session) {
        return ResponseEntity.ok(skinService.setBorder(payload, session));
    }

}
