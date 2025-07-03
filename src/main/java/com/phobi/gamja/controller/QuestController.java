package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.BattleService;
import com.phobi.gamja.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getQuestList(HttpServletRequest request) {
        return questService.getQuestList(request);
    }

    @GetMapping("/chronicle/list")
    public ResponseEntity<GamJaResponse> getChronicleQuestList(HttpServletRequest request) {
        return questService.getChronicleQuestList(request);
    }

    @PostMapping("/complete-quest")
    public ResponseEntity<GamJaResponse> completeQuest(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        return questService.completeQuest(request, payload);
    }
}
