package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.ChronicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/chronicle")
@RequiredArgsConstructor
public class ChronicleController {

    private final ChronicleService chronicleService;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getChronicleList(@RequestParam Long mapId, HttpSession session) {
        return chronicleService.getChronicleList(mapId, session);
    }

    @GetMapping("/progress")
    public ResponseEntity<GamJaResponse> getChronicleProgress(@RequestParam Long mapId, HttpSession session) {
        return chronicleService.getChronicleProgress(mapId, session);
    }


    @PostMapping("/complete")
    public ResponseEntity<GamJaResponse> completeChronicle(HttpSession session, @RequestBody Map<String, Object> request) {
        return chronicleService.completeChronicle(session, request);
    }
}
