package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.ChronicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chronicle")
@RequiredArgsConstructor
public class ChronicleController {

    private final ChronicleService chronicleService;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getChronicleList(@RequestParam Long mapId, HttpSession session) {
        return chronicleService.getChronicleList(mapId, session);
    }
}
