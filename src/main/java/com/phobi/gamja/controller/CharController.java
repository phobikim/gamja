package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.CharService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/char")
public class CharController {

    private final CharService charService;

    @GetMapping("")
    public ResponseEntity<GamJaResponse> getCharInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getUserInfo(request));
    }

    @GetMapping("/battle")
    public ResponseEntity<GamJaResponse> getBattleInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getBattleInfo(request));
    }

    @GetMapping("/life")
    public ResponseEntity<GamJaResponse> getLifeInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getLifeInfo(request));
    }

    @PostMapping("/setDex")
    public ResponseEntity<GamJaResponse> setCharacterImage(@RequestBody Map<String, Long> payload, HttpServletRequest request) {
        return ResponseEntity.ok(charService.setCharacterImage(request, payload));
    }
}
