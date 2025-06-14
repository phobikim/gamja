package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.DexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dex")
public class DexController {

    private final DexService dexService;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getDexMeta(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        return ResponseEntity.ok(dexService.getDexMeta(userId));
    }
}
