package com.phobi.gamja.controller;


import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.ItemAlchemyService;
import com.phobi.gamja.service.ItemEnhanceService;
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
@RequestMapping("/api/alchemy")
public class ItemAlchemyController {

    private final ItemAlchemyService itemAlchemyService;
    @PostMapping("/available-options")
    public ResponseEntity<GamJaResponse> getAvailableAlchemyOptions(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemAlchemyService.getAvailableAlchemyOptions(session, payload);
    }
    @PostMapping("/material")
    public ResponseEntity<GamJaResponse> getAlchemyMaterialInfo(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemAlchemyService.getAlchemyMaterialInfo(session, payload);
    }

    @PostMapping("/execute")
    public ResponseEntity<GamJaResponse> executeAlchemy(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemAlchemyService.executeAlchemy(session, payload);
    }
}
