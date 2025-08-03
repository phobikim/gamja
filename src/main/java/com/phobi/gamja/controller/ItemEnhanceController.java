package com.phobi.gamja.controller;


import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.ItemEnhanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enhance")
public class ItemEnhanceController {

    private final ItemEnhanceService itemEnhanceService;

    @PostMapping("/material")
    public ResponseEntity<GamJaResponse> getEnhanceItemList(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemEnhanceService.getEnhanceItemList(session, payload);
    }

    @PostMapping("/execute")
    public ResponseEntity<GamJaResponse> executeEnhance(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemEnhanceService.executeEnhance(session, payload);
    }

    @PostMapping("/execute-free")
    public ResponseEntity<GamJaResponse> executeFreeEnhance(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemEnhanceService.executeFreeEnhance(session, payload);
    }

    @PostMapping("/transfer-item")
    public ResponseEntity<GamJaResponse> getTransferItemList(
            HttpSession session,
            @RequestBody Map<String, Long> payload
    ) {
        return itemEnhanceService.getTransferItemList(session, payload);
    }
}
